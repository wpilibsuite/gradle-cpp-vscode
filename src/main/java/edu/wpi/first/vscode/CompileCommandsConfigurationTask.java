package edu.wpi.first.vscode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import edu.wpi.first.vscode.tooling.ToolChainGenerator;
import edu.wpi.first.vscode.tooling.models.BinaryObject;
import edu.wpi.first.vscode.tooling.models.CompileCommand;
import edu.wpi.first.vscode.tooling.models.CompileCommandImpl;
import edu.wpi.first.vscode.tooling.models.Source;
import edu.wpi.first.vscode.tooling.models.SourceSet;
import edu.wpi.first.vscode.tooling.models.ToolChains;

public class CompileCommandsConfigurationTask extends DefaultTask {
  private DirectoryProperty configDirectory;

  @Inject
  public CompileCommandsConfigurationTask(ObjectFactory factory) {
    configDirectory = factory.directoryProperty();
  }

  @OutputDirectory
  public DirectoryProperty getConfigDirectory() {
    return configDirectory;
  }

  @Input
  public Set<ToolChains> getToolChains() {
    return ToolChainGenerator.generateToolChains(getProject());
  }

  @TaskAction
  public void generate() {
    Set<ToolChains> toolChains = getToolChains();

    VsCodeConfigurationExtension ext = getProject().getExtensions().getByType(VsCodeConfigurationExtension.class);

    GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();

    File dir = configDirectory.get().getAsFile();

    dir.mkdirs();

    if (ext.getPrettyPrinting()) {
      builder.setPrettyPrinting();
    }

    Gson gson = builder.create();
    List<CompileCommand> compileCommands = new ArrayList<>();
    String rootDirectory = getProject().getRootDir().getAbsolutePath();

    for (ToolChains tc : toolChains) {
      compileCommands.clear();

      for (BinaryObject bin : tc.getBinaries()) {
        for (SourceSet ss : bin.getSourceSets()) {
          Source source = ss.getSource();
          FileTree tree = getProject().files(source.getSrcDirs()).getAsFileTree().matching(p -> {
            p.include(source.getIncludes());
            p.exclude(source.getExcludes());
          });

          for (File f : tree) {
            compileCommands.add(new CompileCommandImpl(rootDirectory, tc, bin, ss, source, f));
          }
        }
      }

      String json = gson.toJson(compileCommands);
      json.replaceAll("\\\\", "/"); // for Windows since clangd doesn't
                                    // support backslash dir separator

      File fileDir = new File(dir, tc.getName());
      fileDir.mkdirs();

      File outputFile = new File(fileDir, "compile_commands.json");

      try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath())) {
        writer.append(json);
      } catch (IOException ex) {

      }
    }


  }
}
