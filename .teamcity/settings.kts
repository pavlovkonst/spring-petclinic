import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs

version = "2024.12"

project {
    description = "Spring PetClinic CI/CD Pipeline."

    buildType(Build)
    buildType(Test)
    buildType(Package)

    params {
        param("maven.opts", "-Xmx1024m")
    }
}

object Build : BuildType({
    name = "Build"
    description = "Compile and validate the Spring PetClinic application"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Maven Validate and Compile"
            goals = "clean compile"
            mavenVersion = bundled_3_9()
            jdkHome = "%env.JDK_17_0%"
            runnerArgs = "-Dmaven.test.skip=true"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    requirements {
        exists("env.JDK_17_0")
    }

    artifactRules = """
        target/*.jar => artifacts/
    """.trimIndent()
})

object Test : BuildType({
    name = "Test"
    description = "Run tests with coverage report"

    vcs {
        root(DslContext.settingsRoot)
    }

    dependencies {
        snapshot(Build) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    steps {
        maven {
            name = "Maven Test"
            goals = "test"
            mavenVersion = bundled_3_9()
            jdkHome = "%env.JDK_17_0%"
            runnerArgs = "-Dmaven.test.failure.ignore=false"
        }
        maven {
            name = "JaCoCo Coverage Report"
            goals = "jacoco:report"
            mavenVersion = bundled_3_9()
            jdkHome = "%env.JDK_17_0%"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    requirements {
        exists("env.JDK_17_0")
    }

    artifactRules = """
        target/surefire-reports/** => test-reports/
        target/site/jacoco/** => coverage-reports/
    """.trimIndent()

    features {
        feature {
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "surefire")
            param("xmlReportParsing.reportDirs", "target/surefire-reports/**/*.xml")
        }
    }
})

object Package : BuildType({
    name = "Package"
    description = "Package the application as JAR"

    vcs {
        root(DslContext.settingsRoot)
    }

    dependencies {
        snapshot(Test) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    steps {
        maven {
            name = "Maven Package"
            goals = "package"
            mavenVersion = bundled_3_9()
            jdkHome = "%env.JDK_17_0%"
            runnerArgs = "-DskipTests=true"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:main"
        }
    }

    requirements {
        exists("env.JDK_17_0")
    }

    artifactRules = """
        target/*.jar => artifacts/
        target/site/jacoco/** => coverage-reports/
    """.trimIndent()
})
