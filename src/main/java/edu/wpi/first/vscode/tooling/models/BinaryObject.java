package edu.wpi.first.vscode.tooling.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BinaryObject {
  public String componentName = "";
  public List<SourceSet> sourceSets = new ArrayList<>();
  public Set<String> libHeaders = new LinkedHashSet<>();
}
