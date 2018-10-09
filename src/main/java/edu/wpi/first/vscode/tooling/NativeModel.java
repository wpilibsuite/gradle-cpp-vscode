package edu.wpi.first.vscode.tooling;

import org.gradle.tooling.model.Model;

public interface NativeModel extends Model {
  String getToolChains();
}
