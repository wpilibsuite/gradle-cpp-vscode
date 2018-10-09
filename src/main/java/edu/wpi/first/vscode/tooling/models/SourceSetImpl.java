package edu.wpi.first.vscode.tooling.models;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class SourceSetImpl implements SourceSet, Serializable {
  private static final long serialVersionUID = 7269670194177298724L;
  public Source source = new SourceImpl();
  public Source exportedHeaders = new SourceImpl();
  public boolean cpp = true;
  public Set<String> args = new LinkedHashSet<>();
  public Set<String> macros = new LinkedHashSet<>();

  @Override
  public Source getSource() {
    return source;
  }

  @Override
  public Source getExportedHeaders() {
    return exportedHeaders;
  }

  @Override
  public boolean getCpp() {
    return cpp;
  }

  @Override
  public Set<String> getArgs() {
    return args;
  }

  @Override
  public Set<String> getMacros() {
    return macros;
  }
}
