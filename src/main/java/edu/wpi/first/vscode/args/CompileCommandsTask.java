package edu.wpi.first.vscode.args;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@UntrackedTask(because = "Always want to rerun")
public abstract class CompileCommandsTask extends DefaultTask {
    public static class CompileCommand {
        public String directory;
        public String file;
        public String command;
    }


    @Internal
    public abstract Property<AbstractNativeSourceCompileTask> getCompileTask();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void execute() throws IOException {
        AbstractNativeSourceCompileTask task = getCompileTask().get();
        NativeCompileSpec spec = NativeCompileSpec.fromCompile(task);

        File ccFile = getOutputDirectory().file("compile_commands.json").get().getAsFile();

        try (BufferedWriter writer = Files.newBufferedWriter(ccFile.toPath())) {
            writer.write("Hello World!");
        }

        List<String> args = spec.getTransformer().transform(spec);
        String argString = args.stream().map(x -> "\"" + x + "\"").collect(Collectors.joining(", "));

        List<CompileCommand> commands = new ArrayList<>();

        for (File f : task.getSource()) {
            CompileCommand c = new CompileCommand();
            c.directory = getProject().getProjectDir().getAbsolutePath();
            c.file = f.getAbsolutePath();
            c.command = argString + "\"" + f.getAbsolutePath() + "\"";
            commands.add(c);
        }

        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String json = gson.toJson(commands);
        try (BufferedWriter writer = Files.newBufferedWriter(ccFile.toPath())) {
            writer.append(json);
        }
    }
}
