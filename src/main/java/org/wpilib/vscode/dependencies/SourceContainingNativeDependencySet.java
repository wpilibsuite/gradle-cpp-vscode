package org.wpilib.vscode.dependencies;

import org.gradle.api.file.FileCollection;

public interface SourceContainingNativeDependencySet {
    FileCollection getSourceRoots();
}
