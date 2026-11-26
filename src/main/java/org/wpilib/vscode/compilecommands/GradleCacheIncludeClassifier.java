package org.wpilib.vscode.compilecommands;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Moves include roots that live in the Gradle user home cache into the system
 * include list, so GCC-like compilers receive them as -isystem instead of -I.
 */
public final class GradleCacheIncludeClassifier {
    private final Path gradleCachePath;

    public GradleCacheIncludeClassifier(File gradleUserHomeDir) {
        this.gradleCachePath = new File(gradleUserHomeDir, "caches")
                .getAbsoluteFile()
                .toPath()
                .normalize();
    }

    /**
     * Adds include roots to the appropriate list. Roots inside the Gradle cache
     * are added to {@code systemIncludes}; everything else stays in
     * {@code userIncludes}.
     */
    public void classify(Iterable<File> includeRoots, List<File> userIncludes, List<File> systemIncludes) {
        for (File includeRoot : includeRoots) {
            if (isInGradleCache(includeRoot)) {
                if (!systemIncludes.contains(includeRoot)) {
                    systemIncludes.add(includeRoot);
                }
            } else {
                userIncludes.add(includeRoot);
            }
        }
    }

    public boolean isInGradleCache(File includeRoot) {
        Path path = includeRoot.getAbsoluteFile().toPath().normalize();
        return path.startsWith(gradleCachePath);
    }
}
