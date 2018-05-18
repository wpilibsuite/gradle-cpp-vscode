package edu.wpi.first.vscode;

import java.util.ArrayList;
import java.util.List;

import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.VisualCppPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.process.internal.ExecActionFactory;

public class VsCodeConfigurationExtension {
  public List<NativeBinarySpec> _binaries = new ArrayList<>();
  public VisualStudioLocator _vsLocator;
  public WindowsSdkLocator _vssdkLocator;
  public UcrtLocator _vsucrtLocator;
  public ExecActionFactory _execActionFactory;
  public List<VisualCppPlatformToolChain> _visualCppPlatforms = new ArrayList<>();
  public List<GccPlatformToolChain> _gccLikePlatforms = new ArrayList<>();

  List<ToolChainConfiguration> configurations = new ArrayList<>();

  String getNameForConfiguration(String architecture, String operatingSystem, String flavor, String buildType) {
    for (ToolChainConfiguration tc : configurations) {
      if (tc.matches4(architecture, operatingSystem, flavor, buildType)) {
        return tc.name;
      }
    }
    for (ToolChainConfiguration tc : configurations) {
      if (tc.matches2(architecture, operatingSystem)) {
        return tc.name;
      }
    }
    for (ToolChainConfiguration tc : configurations) {
      if (tc.matches0()) {
        return tc.name;
      }
    }
    return architecture + operatingSystem + flavor + buildType;
  }

  public void addToolChainName(String name, String architecture, String operatingSystem, String flavor, String buildType) {
    configurations.add(new ToolChainConfiguration(name, architecture, operatingSystem, flavor, buildType));
  }

  public void addToolChainName(String name, String architecture, String operatingSystem) {
    configurations.add(new ToolChainConfiguration(name, architecture, operatingSystem));
  }

  public void addToolChainName(String name) {
    configurations.add(new ToolChainConfiguration(name));
  }
}
