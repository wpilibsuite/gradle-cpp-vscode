package edu.wpi.first.vscode;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.model.Finalize;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.process.internal.ExecActionFactory;

import edu.wpi.first.vscode.compilecommands.BinaryCompileCommandsTask;
import edu.wpi.first.vscode.compilecommands.CompileCommand;

public class GradleVsCodeRules extends RuleSource {
  @Finalize
  void getPlatformToolChains(NativeToolChainRegistry toolChains, ProjectLayout projectLayout) {
    Project project = (Project)projectLayout.getProjectIdentifier();
    Project rootProject = project.getRootProject();
    VsCodeConfigurationExtension ext = rootProject.getExtensions().getByType(VsCodeConfigurationExtension.class);
    toolChains.all(tc -> {
      if (tc instanceof VisualCpp) {
        VisualCpp vtc = (VisualCpp)tc;
        vtc.eachPlatform(t -> {
          ext._visualCppPlatforms.add(t);
        });
      } else if (tc instanceof GccCompatibleToolChain) {
        GccCompatibleToolChain gtc = (GccCompatibleToolChain)tc;
        gtc.eachPlatform(t -> {
            ext._gccLikePlatforms.add(t);
        });
      }
    });
  }

  @Mutate
  void createVsCodeConfigTasks(ModelMap<Task> tasks, BinaryContainer bins, ProjectLayout projectLayout, ServiceRegistry serviceRegistry) {

    CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory.class);

    VisualStudioLocator locator = serviceRegistry.get(VisualStudioLocator.class);
    WindowsSdkLocator sdkLocator = serviceRegistry.get(WindowsSdkLocator.class);
    UcrtLocator ucrtLocator = serviceRegistry.get(UcrtLocator.class);

    ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);

    Project project = (Project)projectLayout.getProjectIdentifier();
    Project rootProject = project.getRootProject();
    VsCodeConfigurationExtension ext = rootProject.getExtensions().getByType(VsCodeConfigurationExtension.class);

    ext._vsLocator = locator;
    ext._vssdkLocator = sdkLocator;
    ext._vsucrtLocator = ucrtLocator;
    ext._execActionFactory = execActionFactory;

    for(BinarySpec oBin : bins) {
      if (!(oBin instanceof NativeBinarySpec)) {
        continue;
      }
      NativeBinarySpec bin = (NativeBinarySpec)oBin;
      ext._binaries.add(bin);

      bin.getTasks().withType(AbstractNativeSourceCompileTask.class, task -> {
        String ccName = task.getName() + "CompileCommands";
        project.getTasks().register(ccName, BinaryCompileCommandsTask.class, ccTask -> {
          ccTask.getCompileTask().set(task);
          ccTask.getOutputNamingFactory().set(compilerOutputFileNamingSchemeFactory);
          ccTask.getBuildType().set(bin.getBuildType());
          ccTask.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(CompileCommand.BINARY_COMPILE_COMMANDS_FOLDER + "/" + ccName));
        });
      });
    }
  }
}
