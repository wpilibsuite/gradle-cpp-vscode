package edu.wpi.first.vscode.tooling.models;

import java.util.List;
import java.util.Set;

public interface BinaryObject {
  String getComponentName();
  List<SourceSet> getSourceSets();
  Set<String> getLibHeaders();
  boolean isSharedLibrary();
  boolean isExecutable();
}
