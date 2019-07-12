package edu.wpi.first.vscode.tooling.models;

public interface CompileCommand {
  public String getDirectory();
  public String getCommand();
  public String getFile();
}
