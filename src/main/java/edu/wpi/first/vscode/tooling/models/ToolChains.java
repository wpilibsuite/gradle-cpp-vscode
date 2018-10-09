package edu.wpi.first.vscode.tooling.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ToolChains {
  public String name;
  public String architecture;
  public String operatingSystem;
  public String flavor;
  public String buildType;
  public String cppPath = "";
  public String cPath = "";
  public boolean msvc = true;

  public Set<String> systemCppMacros = new LinkedHashSet<>();
  public Set<String> systemCppArgs = new LinkedHashSet<>();
  public Set<String> systemCMacros = new LinkedHashSet<>();
  public Set<String> systemCArgs = new LinkedHashSet<>();

  public Set<String> allLibFiles = new LinkedHashSet<>();

  public List<BinaryObject> binaries = new ArrayList<>();

  public Set<SourceBinaryPair> sourceBinaries = new LinkedHashSet<>();

  public Map<String, Integer> nameBinaryMap = new HashMap<>();

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof ToolChains)) {
      return false;
    }

    ToolChains tc = (ToolChains) o;

    return tc.architecture.equals(architecture) && tc.operatingSystem.equals(operatingSystem)
        && tc.flavor.equals(flavor) && tc.buildType.equals(buildType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(architecture, operatingSystem, flavor, buildType);
  }
}
