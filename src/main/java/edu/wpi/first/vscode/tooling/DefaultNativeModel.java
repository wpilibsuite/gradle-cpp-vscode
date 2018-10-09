package edu.wpi.first.vscode.tooling;

import java.io.Serializable;
import java.util.Set;

import edu.wpi.first.vscode.tooling.models.ToolChains;

public class DefaultNativeModel implements NativeModel, Serializable {
  private static final long serialVersionUID = -3717969917994009762L;

  private final Set<ToolChains> toolChains;

  public DefaultNativeModel(Set<ToolChains> toolChains) {
    this.toolChains = toolChains;
  }

  @Override
  public Set<ToolChains> getToolChains() {
    return toolChains;
  }
}
