package edu.wpi.first.vscode.tooling.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class SourceBinaryPairImpl implements SourceBinaryPair, Serializable {
  private static final long serialVersionUID = 6250958742543552175L;

  public SourceBinaryPairImpl(SourceSet ss, Source s, String c, boolean executable, boolean sharedLibrary) {
    this.cpp = ss.getCpp();
    this.args = ss.getArgs();
    this.macros = ss.getMacros();
    this.source = s;
    this.componentName = c;
    this.executable = executable;
    this.sharedLibrary = sharedLibrary;
  }

  public Source source;
  public String componentName;
  public boolean cpp;
  public Set<String> args;
  public Set<String> macros;
  public boolean sharedLibrary;
  public boolean executable;

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof SourceBinaryPair)) {
      return false;
    }

    SourceBinaryPair tc = (SourceBinaryPair) o;

    return tc.getSource().equals(this.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.source);
  }

  @Override
  public Source getSource() {
    return source;
  }

  @Override
  public String getComponentName() {
    return componentName;
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

  @Override
  public boolean isSharedLibrary() {
    return sharedLibrary;
  }

  @Override
  public boolean isExecutable() {
    return executable;
  }
}
