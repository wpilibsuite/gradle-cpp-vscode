package edu.wpi.first.vscode.tooling.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BinaryObjectImpl implements BinaryObject, Serializable {
  private static final long serialVersionUID = 5333886070036739486L;
  public String componentName = "";
  public List<SourceSet> sourceSets = new ArrayList<>();
  public Set<String> libHeaders = new LinkedHashSet<>();
  public boolean sharedLibrary;
  public boolean executable;

  @Override
  public String getComponentName() {
    return componentName;
  }

  @Override
  public List<SourceSet> getSourceSets() {
    return sourceSets;
  }

  @Override
  public Set<String> getLibHeaders() {
    return libHeaders;
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
