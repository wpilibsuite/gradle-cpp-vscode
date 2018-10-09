package edu.wpi.first.vscode.tooling.models;

import java.util.Set;

public interface SourceSet {
  Source getSource();
  Source getExportedHeaders();
  boolean getCpp();
  Set<String> getArgs();
  Set<String> getMacros();
}
