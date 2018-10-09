package edu.wpi.first.vscode.tooling;

import java.util.Set;

import org.gradle.tooling.model.Model;

import edu.wpi.first.vscode.tooling.models.ToolChains;

public interface NativeModel extends Model {
  Set<ToolChains> getToolChains();
}
