package edu.wpi.first.vscode.args;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gradle.nativeplatform.platform.NativePlatform;

/**
 * Maps common options for C/C++ compiling with GCC
 */
public class GccCompilerArgsTransformer implements ArgsTransformer {
    @Override
    public List<String> transform(NativeCompileSpec spec) {
        List<String> args = new ArrayList<>();
        addToolSpecificArgs(spec, args);
        addMacroArgs(spec, args);
        addUserArgs(spec, args);
        addIncludeArgs(spec, args);
        return args;
    }

    protected void addToolSpecificArgs(NativeCompileSpec spec, List<String> args) {
        Collections.addAll(args, "-x", spec.getLanguage());
        args.add("-c");
        if (spec.isPositionIndependentCode()) {
            if (!spec.getTargetPlatform().getOperatingSystem().isWindows()) {
                args.add("-fPIC");
            }
        }
        if (spec.isDebuggable()) {
            args.add("-g");
        }
        if (spec.isOptimized()) {
            args.add("-O3");
        }
    }

    protected void addIncludeArgs(NativeCompileSpec spec, List<String> args) {
        if (!needsStandardIncludes(spec.getTargetPlatform())) {
            args.add("-nostdinc");
        }

        for (File file : spec.getIncludeRoots()) {
            args.add("-I");
            args.add(file.getAbsolutePath());
        }

        for (File file : spec.getSystemIncludeRoots()) {
            args.add("-isystem");
            args.add(file.getAbsolutePath());
        }
    }

    protected void addMacroArgs(NativeCompileSpec spec, List<String> args) {
        for (String macroArg : new MacroArgsConverter().transform(spec.getMacros())) {
            args.add("-D" + macroArg);
        }
    }

    protected void addUserArgs(NativeCompileSpec spec, List<String> args) {
        args.addAll(spec.getAllArgs());
    }

    protected boolean needsStandardIncludes(NativePlatform targetPlatform) {
        return targetPlatform.getOperatingSystem().isMacOsX();
    }
}
