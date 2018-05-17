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
  public List<NativeBinarySpec> binaries = new ArrayList<>();
  public VisualStudioLocator vsLocator;
  public WindowsSdkLocator vssdkLocator;
  public UcrtLocator vsucrtLocator;
  public ExecActionFactory execActionFactory;
  public List<VisualCppPlatformToolChain> visualCppPlatforms = new ArrayList<>();
  public List<GccPlatformToolChain> gccLikePlatforms = new ArrayList<>();
}
