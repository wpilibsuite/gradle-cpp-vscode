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
  private boolean prettyPrint = true;

  public void setPrettyPrinting(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  public boolean getPrettyPrinting() {
    return prettyPrint;
  }
}
