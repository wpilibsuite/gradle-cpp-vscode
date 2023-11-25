# Gradle C++ VS Code

[![CI](https://github.com/wpilibsuite/gradle-cpp-vscode/actions/workflows/main.yml/badge.svg)](https://github.com/wpilibsuite/gradle-cpp-vscode/actions/workflows/main.yml)

This is a Gradle plugin and VS Code extension to enable C++ intellisense for old style C++ gradle projects. New style projects are currently not supported, but could be added in the future.

## VS Code Insructions
For VS Code instructions, see [the VS Code Readme.](extension/README.md)

## Gradle Instructions
To add this to gradle, add the extension to your plugin block. See _insertUrlHere_ for the latest version and instructions how to do this. That is all that needs to be done in Gradle.

Once added, the task `generateVsCodeConfig` is added to the root project, which when ran will generate a `vscodeconfig.json` 

# Using custom builds

To use a custom build of gradle-cpp-vscode in a robot project, the build must be published, and a native-utils and GradleRIO build that uses the new version must be pulished.

1. Update the version in `build.gradle` so that native-utils won't overwrite an existing version.
```
group 'edu.wpi.first'
version '1.3.0'
```
2. Execute `.\gradlew publishToMavenLocal`
3. Update gradle-cpp-vscode version in native-utils ``build.gradle``: ``api 'edu.wpi.first:gradle-cpp-vscode:1.3.0'``
4. Follow the directions in the ![native-utils readme](https://github.com/wpilibsuite/native-utils/blob/main/README.md#using-custom-builds) for publishing a local build and using in a robot program