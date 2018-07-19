package edu.wpi.first.vscode;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;

public class GradleVsCode implements Plugin<Project> {
  public void apply(Project project) {

    if (project.equals(project.getRootProject())) {
      project.getTasks().register("generateVsCodeConfig", VsCodeConfigurationTask.class, task -> {
        task.setGroup("VSCode");
        task.setDescription("Generate configuration file for VSCode");
        task.configFile.set(project.getLayout().getBuildDirectory().file("vscodeconfig.json"));
      });
      project.getExtensions().create("vscodeConfiguration", VsCodeConfigurationExtension.class);
      project.getExtensions().getExtraProperties().set("VsCodeConfigurationTask", VsCodeConfigurationTask.class);
    }

    project.subprojects(subproject -> {
      subproject.apply(config -> {
        config.plugin(GradleVsCode.class);
      });
    });

    project.getPlugins().withType(NativeComponentPlugin.class, a -> {
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
