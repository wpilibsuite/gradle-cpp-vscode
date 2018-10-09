package edu.wpi.first.vscode.tooling;

import java.io.Serializable;

public class DefaultNativeModel implements NativeModel, Serializable {
  private static final long serialVersionUID = -3717969917994009762L;

  private final String toolChains;

  public DefaultNativeModel(String toolChains) {
    this.toolChains = toolChains;
  }

  @Override
  public String getToolChains() {
    return toolChains;
  }
}
