package edu.wpi.first.vscode.tooling.models;

import java.util.Set;

public interface SourceBinaryPair {
  Source getSource();
  String getComponentName();
  boolean getCpp();
  Set<String> getArgs();
  Set<String> getMacros();
}
