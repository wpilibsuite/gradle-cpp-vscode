package edu.wpi.first.vscode.tooling.models;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class SourceImpl implements Source, Serializable {
  private static final long serialVersionUID = 8327004533674695786L;
  public Set<String> srcDirs;
  public Set<String> includes = new LinkedHashSet<>();
  public Set<String> excludes = new LinkedHashSet<>();

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof Source)) {
      return false;
    }

    Source tc = (Source) o;

    return tc.getSrcDirs().equals(this.srcDirs) && tc.getIncludes().equals(this.includes) && tc.getExcludes().equals(this.excludes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.srcDirs, this.includes, this.excludes);
  }

  @Override
  public Set<String> getSrcDirs() {
    return srcDirs;
  }

  @Override
  public Set<String> getIncludes() {
    return includes;
  }

  @Override
  public Set<String> getExcludes() {
    return excludes;
  }

  @Override
  public void setSrcDirs(Set<String> srcDirs) {
    this.srcDirs = srcDirs;
  }
}
