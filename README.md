# Gradle C++ VS Code

[![CI](https://github.com/wpilibsuite/gradle-cpp-vscode/actions/workflows/main.yml/badge.svg)](https://github.com/wpilibsuite/gradle-cpp-vscode/actions/workflows/main.yml)

This is a Gradle plugin and VS Code extension to enable C++ intellisense for old style C++ gradle projects. New style projects are currently not supported, but could be added in the future.

## VS Code Insructions
For VS Code instructions, see [the VS Code Readme.](extension/README.md)

## Gradle Instructions
To add this to gradle, add the extension to your plugin block. See _insertUrlHere_ for the latest version and instructions how to do this. That is all that needs to be done in Gradle.

Once added, the task `generateVsCodeConfig` is added to the root project, which when ran will generate a `vscodeconfig.json` 
