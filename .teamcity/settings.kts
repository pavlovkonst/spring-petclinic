import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon

version = "2024.12"

project {
    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Build"
            goals = "clean compile"
            runnerArgs = "-Dmaven.test.skip=true"
            jdkHome = "%env.JDK_17_0%"
        }
        maven {
            name = "Test"
            goals = "test"
            var jdkHome = "%env.JDK_17_0%"
        }
        maven {
            name = "Package"
            goals = "package"
            runnerArgs = "-DskipTests"
            jdkHome = "%env.JDK_17_0%"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }

    artifactRules = """
        target/*.jar
        target/surefire-reports/**/*.xml
    """.trimIndent()

    requirements {
        exists("env.JDK_17_0")
    }
})
