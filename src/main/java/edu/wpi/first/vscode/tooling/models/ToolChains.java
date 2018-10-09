package edu.wpi.first.vscode.tooling.models;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ToolChains {
  String getName();
  String getArchitecture();
  String getOperatingSystem();
  String getFlavor();
  String getBuildType();
  String getCppPath();
  String getCPath();
  boolean getMsvc();

  Set<String> getSystemCppMacros();
  Set<String> getSystemCppArgs();
  Set<String> getSystemCMacros();
  Set<String> getSystemCArgs();

  Set<String> getAllLibFiles();

  List<BinaryObject> getBinaries();

  Set<SourceBinaryPair> getSourceBinaries();

  Map<String, Integer> getNameBinaryMap();
}
