package edu.wpi.first.vscode.tooling.models;

import java.util.LinkedHashSet;
import java.util.Set;

public class SourceSet {
  public Source source = new Source();
  public Source exportedHeaders = new Source();
  public boolean cpp = true;
  public Set<String> args = new LinkedHashSet<>();
  public Set<String> macros = new LinkedHashSet<>();
}
