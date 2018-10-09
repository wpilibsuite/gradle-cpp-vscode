package edu.wpi.first.vscode.tooling.models;

import java.util.Objects;
import java.util.Set;

public class SourceBinaryPair {
  public SourceBinaryPair(SourceSet ss, Source s, String c) {
    this.cpp = ss.cpp;
    this.args = ss.args;
    this.macros = ss.macros;
    this.source = s;
    this.componentName = c;
  }

  public Source source;
  public String componentName;
  public boolean cpp;
  public Set<String> args;
  public Set<String> macros;

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof SourceBinaryPair)) {
      return false;
    }

    SourceBinaryPair tc = (SourceBinaryPair) o;

    return tc.source.equals(this.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.source);
  }
}
