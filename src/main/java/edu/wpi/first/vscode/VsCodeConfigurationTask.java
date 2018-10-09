package edu.wpi.first.vscode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import edu.wpi.first.vscode.tooling.ToolChainGenerator;

public class VsCodeConfigurationTask extends DefaultTask {
  @OutputFile
  public RegularFileProperty configFile = newOutputFile();

  @TaskAction
  public void generate() {
    String toolChains = ToolChainGenerator.generateToolChains(getProject());

    File file = configFile.getAsFile().get();
    file.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
      writer.append(toolChains);
    } catch (IOException ex) {

    }
  }
}
