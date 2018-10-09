package edu.wpi.first.gradlerio

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class VsCodeInitializationTest extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  File buildFile1
  File buildFile2
  File settingsFile

  def setup() {
    settingsFile = testProjectDir.newFile('settings.gradle')
    buildFile1 = testProjectDir.newFile('build.gradle')
    testProjectDir.newFolder('sub')
    buildFile2 = testProjectDir.newFile('sub/build.gradle')
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
        .withProjectDir(testProjectDir.root)
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
        .withProjectDir(testProjectDir.root)
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
        .withProjectDir(testProjectDir.root)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
    def root = testProjectDir.root
    def configFile = new File(root.toString(), 'build/vscodeConfig.json')
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
        .withProjectDir(testProjectDir.root)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
    def root = testProjectDir.root
    def configFile = new File(root.toString(), 'build/vscodeConfig.json')
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
        .withProjectDir(testProjectDir.root)
        .withArguments('generateVsCodeConfig')
        .withPluginClasspath()
        .build()
    then:
    result.task(':generateVsCodeConfig').outcome == SUCCESS
    def root = testProjectDir.root
    def configFile = new File(root.toString(), 'build/vscodeConfig.json')
    assert configFile.exists()
    println configFile.text
  }
}
