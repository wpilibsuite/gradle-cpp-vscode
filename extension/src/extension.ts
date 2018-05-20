'use strict';
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
//import { GradleConfig } from './gradleconfig';
import { setExtensionContext } from './persistentState';
import { ApiProvider } from './apiprovider';
import { CppToolsApi, CustomConfigurationProvider } from './cppapi';

class ShimTools implements CppToolsApi {
    public providers: CustomConfigurationProvider[] = [];

    registerCustomConfigurationProvider(provider: CustomConfigurationProvider): void {
        this.providers.push(provider);
    }
    didChangeCustomConfiguration(provider: CustomConfigurationProvider): void {
        // noop
    }
}


// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export async function activate(context: vscode.ExtensionContext) {

    // Use the console to output diagnostic information (console.log) and errors (console.error)
    // This line of code will only be executed once when your extension is activated
    console.log('Congratulations, your extension "gradle-vscode-cpp" is now active!');

    setExtensionContext(context);

    const workspaces = vscode.workspace.workspaceFolders;

    const cppToolsApi: ShimTools = new ShimTools();

    const outputChannel: vscode.OutputChannel = vscode.window.createOutputChannel('Gradle VsCode');
    context.subscriptions.push(outputChannel);

    let configLoaders: ApiProvider[] = [];

    if (workspaces !== undefined) {
        for (const wp of workspaces) {
            const configLoader = new ApiProvider(wp, cppToolsApi);
            configLoaders.push(configLoader);
        }
    }


    context.subscriptions.push(vscode.window.onDidChangeActiveTextEditor(async e => {
        if (e === undefined) {
            return;
        }

        for (const c of cppToolsApi.providers) {
            if (await c.canProvideConfiguration(e.document.uri)) {
                const match = await c.provideConfigurations([e.document.uri]);
                console.log(match);
            }
        }
    }));

    context.subscriptions.push(vscode.commands.registerCommand('gradlevscpp.selectToolchain', async () => {
        const workspaces = vscode.workspace.workspaceFolders;

        if (workspaces === undefined) {
            return;
        }

        for (const wp of workspaces) {
            for (const loader of configLoaders) {
                if (wp.uri.fsPath === loader.workspace.uri.fsPath) {
                    await loader.selectToolChain();
                }
            }
        }
    }));

    // The command has been defined in the package.json file
    // Now provide the implementation of the command with  registerCommand
    // The commandId parameter must match the command field in package.json
    let disposable = vscode.commands.registerCommand('gradlevscpp.refreshProperties', async () => {


        const workspaces = vscode.workspace.workspaceFolders;

        if (workspaces === undefined) {
            return;
        }

        for (const wp of workspaces) {
            for (const loader of configLoaders) {
                if (wp.uri.fsPath === loader.workspace.uri.fsPath) {
                    await loader.runGradleRefresh();
                    await loader.loadConfigs();
                }
            }
        }
    });

    context.subscriptions.push(vscode.commands.registerCommand('gradlevscpp.refreshProperties', async () => {


        const workspaces = vscode.workspace.workspaceFolders;

        if (workspaces === undefined) {
            return;
        }

        for (const wp of workspaces) {
            for (const loader of configLoaders) {
                if (wp.uri.fsPath === loader.workspace.uri.fsPath) {
                    await loader.runGradleRefresh(true);
                    await loader.loadConfigs();
                }
            }
        }
    }));


    context.subscriptions.push(disposable);
}

// this method is called when your extension is deactivated
export function deactivate() {
}
