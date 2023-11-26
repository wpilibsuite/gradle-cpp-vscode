package edu.wpi.first.vscode.tooling.models;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.util.Set;

public class CompileCommandImpl implements CompileCommand {
  private final String directory;
  private final String command;
  private final String file;

  public CompileCommandImpl(String rootDirectory, ToolChains tc, BinaryObject binary, SourceSet sourceSet, Source source, File file) {
    if (OperatingSystem.current().isWindows()) {
      this.directory = rootDirectory.replace('\\', '/');
    } else {
      this.directory = rootDirectory;
    }

    StringBuilder cmd = new StringBuilder();
    Set<String> compilerArgs;
    Set<String> compilerMacros;

    cmd.append('"');
    if (sourceSet.getCpp()) {
      if (OperatingSystem.current().isWindows()) {
        cmd.append(tc.getCppPath().replace('\\', '/'));
      } else {
        cmd.append(tc.getCppPath());
      }
      compilerArgs = tc.getSystemCppArgs();
      compilerMacros = tc.getSystemCppMacros();
    } else {
      if (OperatingSystem.current().isWindows()) {
        cmd.append(tc.getCPath().replace('\\', '/'));
      } else {
        cmd.append(tc.getCPath());
      }
      compilerArgs = tc.getSystemCArgs();
      compilerMacros = tc.getSystemCMacros();
    }
    cmd.append('"');

    cmd.append(' ');
    for (String arg : compilerArgs) {
      cmd.append('"');
      cmd.append(arg);
      cmd.append('"');
      cmd.append(' ');

    }

    for (String arg : compilerMacros) {
      cmd.append('"');
      cmd.append("-D");
      cmd.append(arg);
      cmd.append('"');
      cmd.append(' ');
    }

    for (String header : binary.getLibHeaders()) {
      cmd.append('"');
      cmd.append("-I");
      if (OperatingSystem.current().isWindows()) {
        cmd.append(header.replace('\\', '/'));
      } else {
        cmd.append(header);
      }
      cmd.append('"');
      cmd.append(' ');
    }

    for (String header : sourceSet.getExportedHeaders().getSrcDirs()) {
      cmd.append('"');
      cmd.append("-I");
      if (OperatingSystem.current().isWindows()) {
        cmd.append(header.replace('\\', '/'));
      } else {
        cmd.append(header);
      }
      cmd.append('"');
      cmd.append(' ');
    }

    for (String arg : sourceSet.getArgs()) {
      cmd.append('"');
      cmd.append(arg);
      cmd.append('"');
      cmd.append(' ');

    }

    for (String arg : sourceSet.getMacros()) {
      cmd.append('"');
      cmd.append("-D");
      cmd.append(arg);
      cmd.append('"');
      cmd.append(' ');
    }

    cmd.append("-c "); // Don't link.
    cmd.append('"');
    if (OperatingSystem.current().isWindows()) {
      cmd.append(file.getAbsolutePath().replace('\\', '/'));
    } else {
      cmd.append(file.getAbsolutePath());
    }
    cmd.append('"');

    this.command = cmd.toString();
    if (OperatingSystem.current().isWindows()) {
      this.file = file.getAbsolutePath().replace('\\', '/');
    } else {
      this.file = file.getAbsolutePath();
    }
  }

  @Override
  public String getDirectory() {
    return directory;
  }

  @Override
  public String getCommand() {
    return command;
  }

  @Override
  public String getFile() {
    return file;
  }

}
