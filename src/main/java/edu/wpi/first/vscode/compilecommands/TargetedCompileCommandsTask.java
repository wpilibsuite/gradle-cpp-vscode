package edu.wpi.first.vscode.compilecommands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@UntrackedTask(because = "Always want to rerun")
public abstract class TargetedCompileCommandsTask extends DefaultTask {

  @OutputDirectory
  public abstract DirectoryProperty getTargetedCompileCommands();

  @Internal
  public abstract ListProperty<Directory> getBinaryCompileDirectories(); 

  @TaskAction
  public void generate() throws IOException {

    Gson jsonReader = new Gson();

    Map<String, List<CompileCommand>> targetedCommands = new HashMap<>();

    for (Directory binaryDirectory : getBinaryCompileDirectories().get()) {
      File targetPlatformFile = binaryDirectory.file(CompileCommand.TARGET_PLATFORM_FILE_NAME).getAsFile();
      File compileCommandsfile = binaryDirectory.file(CompileCommand.COMPILE_COMMANDS_FILE_NAME).getAsFile();
      String targetPlatform = Files.readString(targetPlatformFile.toPath());

      try (BufferedReader reader = Files.newBufferedReader(compileCommandsfile.toPath())) {
        CompileCommand[] binaryCompileCommands = jsonReader.fromJson(reader, CompileCommand[].class);
        List<CompileCommand> addList = targetedCommands.getOrDefault(targetPlatform, null);
        if (addList == null) {
          addList = new ArrayList<>();
          targetedCommands.put(targetPlatform, addList);
        }
        addList.addAll(Arrays.asList(binaryCompileCommands));
      }
    }

    GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
    builder.setPrettyPrinting();
    Gson gson = builder.create();

    for (Entry<String, List<CompileCommand>> commands : targetedCommands.entrySet()) {
      File targetDirectory = getTargetedCompileCommands().dir(commands.getKey()).get().getAsFile();
      targetDirectory.mkdirs();
      File commandsFile = new File(targetDirectory, CompileCommand.COMPILE_COMMANDS_FILE_NAME);
      try (BufferedWriter writer = Files.newBufferedWriter(commandsFile.toPath())) {
        gson.toJson(commands.getValue(), writer);
      }
    }

  }
}
