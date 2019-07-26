package edu.wpi.first.vscode.tooling.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ToolChainsImpl implements ToolChains, Serializable {
  private static final long serialVersionUID = 6459300371760437579L;
  public String name;
  public String architecture;
  public String operatingSystem;
  public String flavor;
  public String buildType;
  public String cppPath = "";
  public String cPath = "";
  public boolean msvc = true;
  public boolean gcc = false;

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

    return tc.getArchitecture().equals(architecture) && tc.getOperatingSystem().equals(operatingSystem)
        && tc.getFlavor().equals(flavor) && tc.getBuildType().equals(buildType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(architecture, operatingSystem, flavor, buildType);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getArchitecture() {
    return architecture;
  }

  @Override
  public String getOperatingSystem() {
    return operatingSystem;
  }

  @Override
  public String getFlavor() {
    return flavor;
  }

  @Override
  public String getBuildType() {
    return buildType;
  }

  @Override
  public String getCppPath() {
    return cPath;
  }

  @Override
  public String getCPath() {
    return cPath;
  }

  @Override
  public boolean getMsvc() {
    return msvc;
  }

  @Override
  public Set<String> getSystemCppMacros() {
    return systemCppMacros;
  }

  @Override
  public Set<String> getSystemCppArgs() {
    return systemCppArgs;
  }

  @Override
  public Set<String> getSystemCMacros() {
    return systemCMacros;
  }

  @Override
  public Set<String> getSystemCArgs() {
    return systemCArgs;
  }

  @Override
  public Set<String> getAllLibFiles() {
    return allLibFiles;
  }

  @Override
  public List<BinaryObject> getBinaries() {
    return binaries;
  }

  @Override
  public Set<SourceBinaryPair> getSourceBinaries() {
    return sourceBinaries;
  }

  @Override
  public Map<String, Integer> getNameBinaryMap() {
    return nameBinaryMap;
  }

  @Override
  public boolean getGcc() {
    return gcc;
  }
}
