package edu.wpi.first.vscode;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.model.Finalize;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.process.internal.ExecActionFactory;

public class GradleVsCode implements Plugin<Project> {
  public void apply(Project project) {
    project.getTasks().create("generateVsCodeConfig", VsCodeConfigurationTask.class, task -> {
      task.setGroup("VSCode");
      task.setDescription("Generate configuration file for VSCode");
      task.configFile.set(project.getLayout().getBuildDirectory().file("/vscodeconfig.json"));
    });
    project.getExtensions().create("vscodeConfiguration", VsCodeConfigurationExtension.class);
    project.getExtensions().getExtraProperties().set("VsCodeConfigurationTask", VsCodeConfigurationTask.class);
  }

  static class Rules extends RuleSource {
    @Finalize
    void getPlatformToolChains(NativeToolChainRegistry toolChains, ExtensionContainer extCont) {
      VsCodeConfigurationExtension ext = extCont.getByType(VsCodeConfigurationExtension.class);
      for (NativeToolChain tc : toolChains) {
        if (tc instanceof VisualCpp) {
          VisualCpp vtc = (VisualCpp)tc;
          vtc.eachPlatform(t -> {
            ext.visualCppPlatforms.add(t);
          });
        } else if (tc instanceof GccCompatibleToolChain) {
          GccCompatibleToolChain gtc = (GccCompatibleToolChain)tc;
          gtc.eachPlatform(t -> {
              ext.gccLikePlatforms.add(t);
          });
        }
      }
    }

    @Mutate
    void createVsCodeConfigTasks(ModelMap<Task> tasks, BinaryContainer bins, ExtensionContainer extCont, ServiceRegistry serviceRegistry) {

      VisualStudioLocator locator = serviceRegistry.get(VisualStudioLocator.class);
      WindowsSdkLocator sdkLocator = serviceRegistry.get(WindowsSdkLocator.class);
      UcrtLocator ucrtLocator = serviceRegistry.get(UcrtLocator.class);

      ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);

      VsCodeConfigurationExtension ext = extCont.getByType(VsCodeConfigurationExtension.class);

      ext.vsLocator = locator;
      ext.vssdkLocator = sdkLocator;
      ext.vsucrtLocator = ucrtLocator;
      ext.execActionFactory = execActionFactory;

      for(BinarySpec oBin : bins) {
        if (!(oBin instanceof NativeBinarySpec)) {
          continue;
        }
        NativeBinarySpec bin = (NativeBinarySpec)oBin;
        ext.binaries.add(bin);
      }
    }
  }
}
