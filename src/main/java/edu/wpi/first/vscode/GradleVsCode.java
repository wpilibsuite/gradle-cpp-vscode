package edu.wpi.first.vscode;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import edu.wpi.first.vscode.tooling.DefaultNativeModel;
import edu.wpi.first.vscode.tooling.NativeModel;
import edu.wpi.first.vscode.tooling.ToolChainGenerator;

public class GradleVsCode implements Plugin<Project> {
  private final ToolingModelBuilderRegistry registry;

  private static class NativeToolingModelBuilder implements ToolingModelBuilder {
    public boolean canBuild(String modelName) {
      // The default name for a model is the name of the Java interface
      return modelName.equals(NativeModel.class.getName());
    }

    public Object buildAll(String modelName, Project project) {
      VsCodeConfigurationExtension ext = project.getExtensions().getByType(VsCodeConfigurationExtension.class);
      String toolChains = ToolChainGenerator.generateToolChains(ext);
      return new DefaultNativeModel(toolChains);
    }
  }

  @Inject
  public GradleVsCode(ToolingModelBuilderRegistry registry) {
    this.registry = registry;
  }

  public void apply(Project project) {
    registry.register(new NativeToolingModelBuilder());

    project.subprojects(subproject -> {
      subproject.apply(config -> {
        config.plugin(GradleVsCode.class);
      });
    });

    project.getPlugins().withType(NativeComponentPlugin.class, a -> {
      Project rootProject = project.getRootProject();
      VsCodeConfigurationExtension vsce = rootProject.getExtensions().findByType(VsCodeConfigurationExtension.class);
      if (vsce == null) {
        rootProject.getTasks().register("generateVsCodeConfig", VsCodeConfigurationTask.class, task -> {
          task.setGroup("VSCode");
          task.setDescription("Generate configuration file for VSCode");
          task.configFile.set(rootProject.getLayout().getBuildDirectory().file("vscodeconfig.json"));
        });
        rootProject.getExtensions().create("vscodeConfiguration", VsCodeConfigurationExtension.class);
        rootProject.getExtensions().getExtraProperties().set("VsCodeConfigurationTask", VsCodeConfigurationTask.class);
      }
      project.getPluginManager().apply(GradleVsCodeRules.class);
      try {
        project.getTasks().named("generateVsCodeConfig");
      } catch (UnknownTaskException ex) {
        project.getTasks().register("generateVsCodeConfig", Task.class, task -> {
          task.setGroup("VSCode");
          task.setDescription("Shim task to enable project creation");
        });
      }
    });
  }
}
