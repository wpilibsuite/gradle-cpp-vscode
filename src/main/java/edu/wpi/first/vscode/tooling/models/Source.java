package edu.wpi.first.vscode.tooling.models;

import java.util.Set;

public interface Source {
  Set<String> getSrcDirs();
  Set<String> getIncludes();
  Set<String> getExcludes();
}
