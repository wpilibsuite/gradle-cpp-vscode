'use strict';

export interface Source {
  srcDirs: string[];
  includes: string[];
  excludes: string[];
}

export interface SourceSet {
  source: Source;
  exportedHeaders: Source;
  cpp: boolean;
  args: string[];
  macros: { [name: string]: string };
}

export interface Binary {
  componentName: string;
  sourceSets: SourceSet[];
  libHeaders: string[];
}

export interface ToolChain {
  name: string;
  architecture: string;
  operatingSystem: string;
  flavor: string;
  buildType: string;
  cppPath: string;
  cPath: string;
  msvc: boolean;
  systemCppMacros: { [name: string]: string };
  systemCppArgs: string[];
  systemCMacros: { [name: string]: string };
  systemCArgs: string[];
  binaries: Binary[];
}
