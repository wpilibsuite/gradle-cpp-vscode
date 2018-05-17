'use strict';

import * as jsonc from 'jsonc-parser';
import * as vscode from 'vscode';
import * as fs from 'fs';
import { ToolChain } from './jsonformats';

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

export class ConfigLoader {
  private workspace: vscode.WorkspaceFolder;

  private statusBar: vscode.StatusBarItem;

  private readonly configFileGlob: string = '**/vscodeconfig.json';

  constructor(workspace: vscode.WorkspaceFolder) {
    this.workspace = workspace;

    this.statusBar = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Right, 2);
    this.statusBar.text = 'none';
  }

  public async loadConfigs(): Promise<void> {
    const files = await vscode.workspace.findFiles(this.configFileGlob, this.workspace.uri.fsPath);
    const promiseArray: Promise<string>[] = [];
    for (const file of files) {
      promiseArray.push(promisifyReadFile(file.fsPath));
    }
    const readFiles: string[] = await Promise.all(promiseArray);

    const toolchains: ToolChain[] = [];

    for (const file of readFiles) {
      const newToolchains: ToolChain[] = jsonc.parse(file);
      for (const newToolChain of newToolchains) {
        let found = false;
        for (const existingChain of toolchains) {
          if (newToolChain.architecture === existingChain.architecture &&
            newToolChain.operatingSystem === existingChain.operatingSystem &&
            newToolChain.flavor === existingChain.flavor &&
            newToolChain.buildType === existingChain.buildType) {
            found = true;
            existingChain.binaries.push(...newToolChain.binaries);
          }
        }
        if (!found) {
          toolchains.push(newToolChain);
        }
      }
    }
  }
}
