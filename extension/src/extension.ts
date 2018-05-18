'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import { ConfigLoader } from './configloader';

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export async function activate(context: vscode.ExtensionContext) {

    // Use the console to output diagnostic information (console.log) and errors (console.error)
    // This line of code will only be executed once when your extension is activated
    console.log('Congratulations, your extension "gradle-vscode-cpp" is now active!');





    const workspaces = vscode.workspace.workspaceFolders;

    let configLoaders: ConfigLoader[] = [];
    let promises: Promise<void>[] = [];

    if (workspaces !== undefined) {
        for (const wp of workspaces) {
            const configLoader = new ConfigLoader(wp);
            configLoaders.push(configLoader);
            promises.push(configLoader.loadConfigs());
        }
    }

    await Promise.all(promises);

    context.subscriptions.push(vscode.commands.registerCommand('gradlevscpp.selectToolchain', async () => {
    }));

    // The command has been defined in the package.json file
    // Now provide the implementation of the command with  registerCommand
    // The commandId parameter must match the command field in package.json
    let disposable = vscode.commands.registerCommand('gradlevscpp.refreshProperties', async () => {
        // The code you place here will be executed every time your command is executed

        const workspaces = vscode.workspace.workspaceFolders;

        let configLoaders: ConfigLoader[] = [];
        let promises: Promise<void>[] = [];

        if (workspaces !== undefined) {
            for (const wp of workspaces) {
                const configLoader = new ConfigLoader(wp);
                configLoaders.push(configLoader);
                promises.push(configLoader.loadConfigs());
            }
        }

        await Promise.all(promises);
    });


    context.subscriptions.push(disposable);
}

// this method is called when your extension is deactivated
export function deactivate() {
}
