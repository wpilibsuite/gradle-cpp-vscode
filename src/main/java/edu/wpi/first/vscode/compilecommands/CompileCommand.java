package edu.wpi.first.vscode.compilecommands;

import java.util.List;

public class CompileCommand {
    public static final String COMPILE_COMMANDS_FILE_NAME = "compile_commands.json";
    public static final String TARGET_PLATFORM_FILE_NAME = "target_platform.txt";
    public static final String BINARY_COMPILE_COMMANDS_FOLDER = "BinaryCompileCommands";
    public static final String TARGETED_COMPILE_COMMANDS_FOLDER = "TargetedCompileCommands";

    public String directory;
    public String file;
    public List<String> arguments;
    public String output;
}
