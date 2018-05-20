# Gradle C++ VS Code

[![Build status](https://ci.appveyor.com/api/projects/status/70287abkmu24tgou/branch/master?svg=true)](https://ci.appveyor.com/project/ThadHouse/gradle-cpp-vscode/branch/master)

This is a Gradle plugin and VS Code extension to enable C++ intellisense for old style C++ gradle projects. New style projects are currently not supported, but could be added in the future.

## VS Code Insructions
For VS Code instructions, see [the VS Code Readme.](extension/README.md)

## Gradle Instructions
To add this to gradle, add the extension to your plugin block. See _insertUrlHere_ for the latest version and instructions how to do this. That is all that needs to be done in Gradle.

Once added, the task `generateVsCodeConfig` is added to the root project, which when ran will generate a `vscodeconfig.json` 
