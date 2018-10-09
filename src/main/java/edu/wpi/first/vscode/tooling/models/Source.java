package edu.wpi.first.vscode.tooling.models;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Source {
  public Set<String> srcDirs = new LinkedHashSet<>();
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

    return tc.srcDirs.equals(this.srcDirs) && tc.includes.equals(this.includes) && tc.excludes.equals(this.excludes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.srcDirs, this.includes, this.excludes);
  }
}
