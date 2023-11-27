package edu.wpi.first.vscode.compilecommands;

import static edu.wpi.first.vscode.compilecommands.EscapeUserArgs.escapeUserArg;
import static edu.wpi.first.vscode.compilecommands.EscapeUserArgs.escapeUserArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VisualCppCompilerArgsTransformer implements ArgsTransformer {
    @Override
    public List<String> transform(NativeCompileSpec spec) {
        List<String> args = new ArrayList<>();
        addToolSpecificArgs(spec, args);
        addMacroArgs(spec, args);
        addUserArgs(spec, args);
        addIncludeArgs(spec, args);
        return args;
    }

    private void addUserArgs(NativeCompileSpec spec, List<String> args) {
        args.addAll(escapeUserArgs(spec.getAllArgs()));
    }

    protected void addToolSpecificArgs(NativeCompileSpec spec, List<String> args) {
        String languageOption = spec.getLanguage();
        if (languageOption != null) {
            args.add(languageOption);
        }
        args.add("/nologo");
        args.add("/c");
        if (spec.isDebuggable()) {
            args.add("/Zi");
        }
        if (spec.isOptimized()) {
            args.add("/O2");
        }
    }

    protected void addIncludeArgs(NativeCompileSpec spec, List<String> args) {
        for (File file : spec.getIncludeRoots()) {
            args.add("/I" + file.getAbsolutePath());
        }
        for (File file : spec.getSystemIncludeRoots()) {
            args.add("/I" + file.getAbsolutePath());
        }
    }

    protected void addMacroArgs(NativeCompileSpec spec, List<String> args) {
        for (String macroArg : new MacroArgsConverter().transform(spec.getMacros())) {
            args.add(escapeUserArg("/D" + macroArg));
        }
    }
}
