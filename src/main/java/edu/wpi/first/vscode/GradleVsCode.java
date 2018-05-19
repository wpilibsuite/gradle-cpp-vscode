package edu.wpi.first.vscode;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;

public class GradleVsCode implements Plugin<Project> {
  public void apply(Project project) {

    if (project.equals(project.getRootProject())) {
      project.getTasks().create("generateVsCodeConfig", VsCodeConfigurationTask.class, task -> {
        task.setGroup("VSCode");
        task.setDescription("Generate configuration file for VSCode");
        task.configFile.set(project.getLayout().getBuildDirectory().file("/vscodeconfig.json"));
      });
      project.getExtensions().create("vscodeConfiguration", VsCodeConfigurationExtension.class);
      project.getExtensions().getExtraProperties().set("VsCodeConfigurationTask", VsCodeConfigurationTask.class);
    }

    project.getPlugins().withType(NativeComponentPlugin.class, a -> {
      project.getPluginManager().apply(GradleVsCodeRules.class);
      if (project.getTasks().findByName("generateVsCodeConfig") == null) {
        project.getTasks().create("generateVsCodeConfig", Task.class, task -> {
          task.setGroup("VSCode");
          task.setDescription("Shim task to enable project creation");
        });
      }
    });
  }
}
