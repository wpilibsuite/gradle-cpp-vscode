package edu.wpi.first.gradlerio

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification

class VsCodeInitializationTest extends Specification {
  @TempDir File testProjectDir
  File buildFile1
  File buildFile2
  File settingsFile

  def setup() {
    settingsFile = new File(testProjectDir, 'settings.gradle')
    buildFile1 = new File(testProjectDir, 'build.gradle')
    new File(testProjectDir, 'sub').mkdirs()
    buildFile2 = new File(testProjectDir, 'sub/build.gradle')
  }

  def "Root Project Initializes Correctly"() {
    given:
    buildFile1 << """
plugins {
  id 'cpp'
  id 'edu.wpi.first.GradleVsCode'
}
"""
    settingsFile << ""
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
  }
  def "Sub Project Initializes Correctly"() {
    given:
    buildFile1 << ""
    buildFile2 << """
plugins {
  id 'cpp'
  id 'edu.wpi.first.GradleVsCode'
}
"""
    settingsFile << "include 'sub'"
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
  }

  def "Sub Project Uses Extension Correctly"() {
    given:
    buildFile1 << ""
    buildFile2 << """
plugins {
  id 'cpp'
  id 'edu.wpi.first.GradleVsCode'
}

model {
  components {
    myLib(NativeExecutableSpec) {

    }
  }
}
"""
    settingsFile << "include 'sub'"
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
    def root = testProjectDir
    def configFile = new File(root.toString(), 'build/vscodeconfig.json')
    assert configFile.exists()
  }

    def "Root Project Uses Extension Correctly"() {
    given:
    buildFile1 << """
plugins {
  id 'cpp'
  id 'edu.wpi.first.GradleVsCode'
}

model {
  components {
    myLib(NativeExecutableSpec) {

    }
  }
}
"""
    settingsFile << ""
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
    def root = testProjectDir
    def configFile = new File(root.toString(), 'build/vscodeconfig.json')
    assert configFile.exists()
  }
    def "Sub Project Uses Extension Correctly"() {
    given:
    buildFile1 << """
plugins {
  id 'cpp'
  id 'edu.wpi.first.GradleVsCode'
}

model {
  components {
    myLibRoot(NativeExecutableSpec) {

    }
  }
}
"""
    buildFile2 << """
plugins {
  id 'cpp'
  id 'edu.wpi.first.GradleVsCode'
}

model {
  components {
    myLib(NativeExecutableSpec) {

    }
  }
}
"""
    settingsFile << "include 'sub'"
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
    def root = testProjectDir
    def configFile = new File(root.toString(), 'build/vscodeconfig.json')
    assert configFile.exists()
    println configFile.text
  }
}
