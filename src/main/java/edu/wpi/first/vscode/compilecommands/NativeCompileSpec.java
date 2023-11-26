package edu.wpi.first.vscode.compilecommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gradle.language.c.tasks.CCompile;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;
import org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingScheme;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

public class NativeCompileSpec {
    private String language;
    private boolean debuggable;
    private boolean positionIndependentCode;
    private boolean optimized;
    private NativePlatform targetPlatform;
    private final List<File> includeRoots = new ArrayList<>();
    private final List<File> systemIncludeRoots = new ArrayList<>();
    private Map<String, String> macros;
    private final List<String> args = new ArrayList<>();
    private final List<String> systemArgs = new ArrayList<>();

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    public boolean isPositionIndependentCode() {
        return positionIndependentCode;
    }

    public void setPositionIndependentCode(boolean positionIndependentCode) {
        this.positionIndependentCode = positionIndependentCode;
    }

    public boolean isOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public NativePlatform getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(NativePlatform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public List<File> getIncludeRoots() {
        return includeRoots;
    }

    public void include(Iterable<File> includeRoots) {
        addAll(this.includeRoots, includeRoots);
    }

    public List<File> getSystemIncludeRoots() {
        return systemIncludeRoots;
    }

    public void systemInclude(Iterable<File> systemIncludeRoots) {
        addAll(this.systemIncludeRoots, systemIncludeRoots);
    }

    public Map<String, String> getMacros() {
        return macros;
    }

    public void setMacros(Map<String, String> macros) {
        this.macros = macros;
    }

    public List<String> getAllArgs() {
        List<String> allArgs = new ArrayList<String>(systemArgs.size() + args.size());
        allArgs.addAll(systemArgs);
        allArgs.addAll(args);
        return allArgs;
    }

    public void args(Iterable<String> args) {
        addAll(this.args, args);
    }

    public void systemArgs(Iterable<String> systemArgs) {
        addAll(this.systemArgs, systemArgs);
    }

    private <T> void addAll(List<T> list, Iterable<T> iterable) {
        for (T file : iterable) {
            list.add(file);
        }
    }

    private ArgsTransformer transformer;

    public ArgsTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(ArgsTransformer transformer) {
        this.transformer = transformer;
    }

    private ToolType toolType;

    public ToolType getToolType() {
        return toolType;
    }

    private File compileTool;

    public File getCompileTool() {
        return compileTool;
    }

    private CompilerOutputFileNamingScheme namingScheme;

    public CompilerOutputFileNamingScheme getNamingScheme() {
        return namingScheme;
    }

    public static NativeCompileSpec fromCompile(AbstractNativeSourceCompileTask task,
            CompilerOutputFileNamingSchemeFactory outputNamingFactory) {
        NativeCompileSpec spec = new NativeCompileSpec();
        spec.setTargetPlatform(task.getTargetPlatform().get());
        spec.include(task.getIncludes());
        spec.systemInclude(task.getSystemIncludes());
        spec.setMacros(task.getMacros());
        spec.args(task.getCompilerArgs().get());
        spec.setPositionIndependentCode(task.isPositionIndependentCode());
        spec.setDebuggable(task.isDebuggable());
        spec.setOptimized(task.isOptimized());

        if (task.getToolChain().get() instanceof VisualCpp) {
            spec.setTransformer(new VisualCppCompilerArgsTransformer());
            if (task instanceof CCompile) {
                spec.toolType = ToolType.C_COMPILER;
                spec.setLanguage("/TC");
            } else if (task instanceof CppCompile) {
                spec.toolType = ToolType.CPP_COMPILER;
                spec.setLanguage("/TP");
            } else {
                throw new RuntimeException("Unknown compiler type");
            }
        } else {
            spec.setTransformer(new GccCompilerArgsTransformer());
            if (task instanceof ObjectiveCppCompile) {
                spec.toolType = ToolType.OBJECTIVECPP_COMPILER;
                spec.setLanguage("objective-c++");
            } else if (task instanceof ObjectiveCCompile) {
                spec.toolType = ToolType.OBJECTIVEC_COMPILER;
                spec.setLanguage("objective-c");
            } else if (task instanceof CCompile) {
                spec.toolType = ToolType.C_COMPILER;
                spec.setLanguage("c");
            } else if (task instanceof CppCompile) {
                spec.toolType = ToolType.CPP_COMPILER;
                spec.setLanguage("c++");
            } else {
                throw new RuntimeException("Unknown compiler type");
            }
        }

        NativeToolChainInternal toolChain = (NativeToolChainInternal) task.getToolChain().get();
        NativePlatformInternal targetPlatform = (NativePlatformInternal) task.getTargetPlatform().get();
        PlatformToolProvider toolProvider = toolChain.select(targetPlatform);
        spec.compileTool = toolProvider.locateTool(spec.getToolType()).getTool();

        spec.namingScheme = outputNamingFactory.create().withObjectFileNameSuffix(toolProvider.getObjectFileExtension())
                .withOutputBaseFolder(task.getObjectFileDir().get().getAsFile());

        return spec;
    }
}
