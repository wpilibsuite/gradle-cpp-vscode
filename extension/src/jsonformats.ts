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
}

export interface Binary {
  componentName: string;

}

export interface ToolChain {
  architecture: string;
  operatingSystem: string;
  flavor: string;
  buildType: string;
  cppPath: string;
  cPath: string;
  msvc: boolean;
  systemCppIncludes: string[];
  systemCppMacros: string[];
  systemCppArgs: string[];
  systemCIncludes: string[];
  systemCMacros: string[];
  systemCArgs: string[];
  binaries: Binary[];
}
