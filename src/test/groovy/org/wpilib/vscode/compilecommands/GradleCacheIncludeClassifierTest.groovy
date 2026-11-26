package org.wpilib.vscode.compilecommands

import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.OperatingSystem
import spock.lang.Specification
import spock.lang.TempDir

class GradleCacheIncludeClassifierTest extends Specification {
  @TempDir File gradleUserHome

  def 'gradle cache include roots are emitted with -isystem and project roots with -I'() {
    given:
    File cacheInclude = new File(gradleUserHome, 'caches/modules-2/files-2.1/com.example/lib/1.0/abc/include')
    File projectInclude = new File(gradleUserHome.parentFile, 'project/include')
    cacheInclude.mkdirs()
    projectInclude.mkdirs()

    NativePlatform platform = Mock(NativePlatform)
    platform.operatingSystem >> Mock(OperatingSystem)

    NativeCompileSpec spec = new NativeCompileSpec()
    spec.targetPlatform = platform
    spec.language = 'c++'
    spec.macros = [:]

    when:
    new GradleCacheIncludeClassifier(gradleUserHome).classify(
        [cacheInclude, projectInclude], spec.includeRoots, spec.systemIncludeRoots)
    List<String> args = new GccCompilerArgsTransformer().transform(spec)

    then:
    args.count('-isystem') == 1
    args[args.indexOf('-isystem') + 1] == cacheInclude.absolutePath
    args.count('-I') == 1
    args[args.indexOf('-I') + 1] == projectInclude.absolutePath
  }

  def 'a root already in the system include list is not added twice'() {
    given:
    File cacheInclude = new File(gradleUserHome, 'caches/modules-2/files-2.1/com.example/lib/1.0/abc/include')
    List<File> userIncludes = []
    List<File> systemIncludes = [cacheInclude]

    when:
    new GradleCacheIncludeClassifier(gradleUserHome).classify([cacheInclude], userIncludes, systemIncludes)

    then:
    userIncludes.isEmpty()
    systemIncludes == [cacheInclude]
  }

  def 'only the caches directory itself is treated as the Gradle cache'() {
    given:
    GradleCacheIncludeClassifier classifier = new GradleCacheIncludeClassifier(gradleUserHome)

    expect:
    classifier.isInGradleCache(new File(gradleUserHome, 'caches/modules-2/files-2.1/include'))
    !classifier.isInGradleCache(new File(gradleUserHome, 'caches-other/include'))
    !classifier.isInGradleCache(new File(gradleUserHome, 'project/include'))
  }
}
