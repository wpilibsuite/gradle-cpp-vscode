package edu.wpi.first.vscode;

public class ToolChainConfiguration {
  public String name;
  public String architecture;
  public String operatingSystem;
  public String flavor;
  public String buildType;
  public boolean is4 = false;
  public boolean is2 = false;

  public boolean matches4(String architecture, String operatingSystem, String flavor, String buildType) {
    if (!is4) {
      return false;
    }
    return this.architecture.equals(architecture) &&
           this.operatingSystem.equals(operatingSystem) &&
           this.flavor.equals(flavor) &&
           this.buildType.equals(buildType);
  }

  public boolean matches2(String architecture, String operatingSystem) {
    if (!is2 || is4) {
      return false;
    }
    return this.architecture.equals(architecture) &&
           this.operatingSystem.equals(operatingSystem);
  }

  public boolean matches0() {
    return !is4 && !is2;
  }

  public ToolChainConfiguration(String name, String architecture, String operatingSystem, String flavor, String buildType) {
    this.name = name;
    this.architecture = architecture;
    this.operatingSystem = operatingSystem;
    this.flavor = flavor;
    this.buildType = buildType;
    is4 = true;
    is2 = true;
  }

  public ToolChainConfiguration(String name, String architecture, String operatingSystem) {
    this(name, architecture, operatingSystem, "", "");
    is4 = false;
    is2 = true;
  }

  public ToolChainConfiguration(String name) {
    this(name, "", "");
    is4 = false;
    is2 = false;
  }
}
