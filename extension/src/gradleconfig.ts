'use strict';

import * as jsonc from 'jsonc-parser';
import * as vscode from 'vscode';
import * as fs from 'fs';
import { ToolChain, Binary, Source } from './jsonformats';
import * as path from 'path';
import * as glob from 'glob';

function promisifyReadFile(location: string): Promise<string> {
  return new Promise<string>((resolve, reject) => {
    fs.readFile(location, 'utf8', (err, data) => {
      if (err) {
        reject(err);
      } else {
        resolve(data);
      }
    });
  });
}

export interface BinaryFind {
  binary: Binary;
  cpp: boolean;
  compiler: string;
  msvc: boolean;
  macros: { [name: string]: string };
  args: string[];
  uri: vscode.Uri;
}

const isWindows = (process.platform === 'win32');

function hasDriveLetter(pth: string): boolean {
  return isWindows && pth[1] === ':';
}

export function normalizeDriveLetter(path: string): string {
  if (hasDriveLetter(path)) {
    return path.charAt(0).toUpperCase() + path.slice(1);
  }

  return path;
}

export class GradleConfig {
  public workspace: vscode.WorkspaceFolder;
  private toolchains: ToolChain[] = [];
  private selectedName: string = 'none';
  private disposables: vscode.Disposable[] = [];

  private foundFiles: BinaryFind[] = [];

  private statusBar: vscode.StatusBarItem;

  private readonly configFileGlob: string = '**/build/vscodeconfig.json';

  private configRelativePattern: vscode.RelativePattern;
  private configWatcher: vscode.FileSystemWatcher;

  constructor(workspace: vscode.WorkspaceFolder) {
    this.workspace = workspace;

    this.configRelativePattern = new vscode.RelativePattern(workspace, this.configFileGlob);

    this.configWatcher = vscode.workspace.createFileSystemWatcher(this.configRelativePattern);

    this.disposables.push(this.configWatcher);

    this.statusBar = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Right, 2);
    this.statusBar.text = this.selectedName;
    this.statusBar.tooltip = 'Click to change toolchain';
    this.statusBar.command = 'gradlevscpp.selectToolchain';

    this.disposables.push(this.statusBar);
  }

  private enumerateSourceSet2(source: Source): Thenable<vscode.Uri[]>[] {
    const promises: Thenable<vscode.Uri[]>[] = [];
    for (const s of source.srcDirs) {
      let includes: string = '**/*';
      if (source.includes.length === 0) {
        includes = '{';
        let first = true;
        for (const i of source.includes) {
          if (first) {
            first = false;
          } else {
            includes += ',';
          }
          includes += i;
        }
        includes += '}';
      }
      let ip = new vscode.RelativePattern(s, includes);
      let excludes: string = '';
      if (source.excludes.length === 0) {
        excludes = '{';
        let first = true;
        for (const i of source.excludes) {
          if (first) {
            first = false;
          } else {
            excludes += ',';
          }
          excludes += i;
        }
        excludes += '}';
      }
      promises.push(vscode.workspace.findFiles(ip, excludes));
    }
    return promises;
  }

  private enumerateSourceSet(source: Source): Promise<string[]>[] {
    const promises: Promise<string[]>[] = [];
    for (const s of source.srcDirs) {
      let includes: string = '**/*';
      if (source.includes.length === 0) {
        includes = '{';
        let first = true;
        for (const i of source.includes) {
          if (first) {
            first = false;
          } else {
            includes += ',';
          }
          includes += i;
        }
        includes += '}';
      }

      let excludes: string = '';
      if (source.excludes.length === 0) {
        excludes = '{';
        let first = true;
        for (const i of source.excludes) {
          if (first) {
            first = false;
          } else {
            excludes += ',';
          }
          excludes += i;
        }
        excludes += '}';
      }
      promises.push(new Promise<string[]>((resolve, reject) => {
        glob(includes, {
          cwd: s,
          ignore: excludes,
          nodir: true
        }, (err, data) => {
          if (err) {
            reject(err);
          } else {
            const newArr: string[] = [];
            for (const d of data) {
              newArr.push(path.join(s, d));
            }
            resolve(newArr);
          }
        });
      }));
    }
    return promises;
  }

  public async findMatchingBinary(uris: vscode.Uri[]): Promise<BinaryFind[]> {

    let findCount = 0;
    const finds: BinaryFind[] = [];

    for (let i = 0; i < uris.length; i++) {
      // Remove non c++ files
      const end1 = uris[i].fsPath.endsWith('.cpp');
      const end2 = uris[i].fsPath.endsWith('.hpp');
      const end3 = uris[i].fsPath.endsWith('.cc');
      const end4 = uris[i].fsPath.endsWith('.hh');
      const end5 = uris[i].fsPath.endsWith('.c');
      const end6 = uris[i].fsPath.endsWith('.h');

      if (!end1 && !end2
       && !end3 && !end4
       && !end5 && !end6) {
        uris.splice(i, 1);
        continue;
      }
      for (const f of this.foundFiles) {
        if (f.uri.fsPath === uris[i].fsPath) {
          findCount++;
          finds.push(f);
          uris.splice(i, 1);
          break;
        }
      }
    }

    if (uris.length === 0) {
      return finds;
    }

    for (const tc of this.toolchains) {
      if (tc.name === this.selectedName) {
        for (const bin of tc.binaries) {
          for (const sourceSet of bin.sourceSets) {
            const arr: Promise<string[]>[] = [];
            arr.push(...this.enumerateSourceSet(sourceSet.source));
            arr.push(...this.enumerateSourceSet(sourceSet.exportedHeaders));
            const matches = await Promise.all(arr);
            for (const set of matches) {
              for (const file of set) {
                for (const uri of uris) {
                  if (normalizeDriveLetter(uri.fsPath) === normalizeDriveLetter(file)) {
                    findCount++;
                    if (sourceSet.cpp) {
                      const args: string[] = [];
                      args.push(...tc.systemCppArgs);
                      args.push(...sourceSet.args);
                      finds.push({
                        args: args,
                        binary: bin,
                        compiler: tc.cppPath,
                        cpp: true,
                        macros: tc.systemCppMacros,
                        msvc: tc.msvc,
                        uri: uri
                      });
                    } else {
                      const args: string[] = [];
                      args.push(...tc.systemCArgs);
                      args.push(...sourceSet.args);
                      finds.push({
                        args: args,
                        binary: bin,
                        compiler: tc.cppPath,
                        cpp: false,
                        macros: tc.systemCMacros,
                        msvc: tc.msvc,
                        uri: uri
                      });
                    }
                    if (findCount === uris.length) {
                      this.foundFiles.push(...finds);
                      return finds;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    this.foundFiles.push(...finds);
    return finds;
  }

  public async loadConfigs(): Promise<void> {
    const files = await vscode.workspace.findFiles(this.configFileGlob, path.join(this.workspace.uri.fsPath, 'build'));
    const promiseArray: Promise<string>[] = [];
    for (const file of files) {
      promiseArray.push(promisifyReadFile(file.fsPath));
    }
    const readFiles: string[] = await Promise.all(promiseArray);

    this.toolchains = [];

    for (const file of readFiles) {
      const newToolchains: ToolChain[] = jsonc.parse(file);
      for (const newToolChain of newToolchains) {
        let found = false;
        for (const existingChain of this.toolchains) {
          if (newToolChain.architecture === existingChain.architecture &&
            newToolChain.operatingSystem === existingChain.operatingSystem &&
            newToolChain.flavor === existingChain.flavor &&
            newToolChain.buildType === existingChain.buildType) {
            found = true;
            existingChain.binaries.push(...newToolChain.binaries);
          }
        }
        if (!found) {
          this.toolchains.push(newToolChain);
        }
      }
    }

    if (this.selectedName === 'none') {
      this.selectedName = this.toolchains[0].name;
      this.statusBar.text = this.selectedName;
    }

    let found = false;
    for (const t of this.toolchains) {
      if (t.name === this.selectedName) {
        found = true;
      }
    }

    if (!found) {
      this.selectedName = this.toolchains[0].name;
      this.statusBar.text = this.selectedName;
    }

    this.foundFiles = [];

    this.statusBar.show();
  }

  public async selectToolChain(): Promise<void> {
    const selections: string[] = [];
    for (const c of this.toolchains) {
      selections.push(c.name);
    }
    if (selections.length === 0) {
      vscode.window.showInformationMessage('No configuration. Try refreshing the configurations');
      return;
    }
    const result = await vscode.window.showQuickPick(selections, {
      placeHolder: 'Pick a configuration'
    });
    if (result !== undefined) {
      this.selectedName = result;
      this.statusBar.text = result;
    }
  }

  public dispose() {
    for (const d of this.disposables) {
      d.dispose();
    }
  }
}
