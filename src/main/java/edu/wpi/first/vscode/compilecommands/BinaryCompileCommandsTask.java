package edu.wpi.first.vscode.compilecommands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@UntrackedTask(because = "Always want to rerun")
public abstract class BinaryCompileCommandsTask extends DefaultTask {

    @Internal
    public abstract Property<AbstractNativeSourceCompileTask> getCompileTask();

    @Internal
    public abstract Property<CompilerOutputFileNamingSchemeFactory> getOutputNamingFactory();

    @Internal
    public abstract Property<BuildType> getBuildType();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void execute() throws IOException {
        AbstractNativeSourceCompileTask task = getCompileTask().get();
        NativeCompileSpec spec = NativeCompileSpec.fromCompile(task, getOutputNamingFactory().get(), getBuildType().get());

        File ccFile = getOutputDirectory().file(CompileCommand.COMPILE_COMMANDS_FILE_NAME).get().getAsFile();

        List<String> args = spec.getTransformer().transform(spec);
        List<CompileCommand> commands = new ArrayList<>();

        for (File f : task.getSource()) {
            CompileCommand c = new CompileCommand();
            c.directory = getProject().getProjectDir().getAbsolutePath();
            c.file = f.getAbsolutePath();
            c.arguments = new ArrayList<>(args.size() + 1);
            c.arguments.add(spec.getCompileTool().getAbsolutePath());
            c.arguments.addAll(args);
            c.output = spec.getNamingScheme().map(f).getAbsolutePath();
            commands.add(c);
        }

        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String json = gson.toJson(commands);
        Files.writeString(ccFile.toPath(), json);

        File platformFile = getOutputDirectory().file(CompileCommand.TARGET_PLATFORM_FILE_NAME).get().getAsFile();
        Files.writeString(platformFile.toPath(), spec.getTargetPlatform().getName() + spec.getBuildType().getName());
    }
}
