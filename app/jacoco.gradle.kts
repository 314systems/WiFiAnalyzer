/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

apply(plugin = "jacoco")

extensions.configure<JacocoPluginExtension> {
    toolVersion = "0.8.14"
}

val executionPath = "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"

val fileFilter =
    listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/databinding/*.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/com/jjoe64/*",
        "**/com/vrem/wifianalyzer/settings/SharedPreferences*",
        "**/*\$DefaultImpls.class",
    )

val classJavaDir =
    layout.buildDirectory.dir(
        "intermediates/javac/debug/compileDebugJavaWithJavac/classes",
    )
val classKotlinDir =
    layout.buildDirectory.dir(
        "tmp/kotlin-classes/debug",
    )
val mainJavaSrc = project.file("src/main/java")
val mainKotlinSrc = project.file("src/main/kotlin")
val debugTree =
    fileTree(classJavaDir) {
        exclude(fileFilter)
    } +
        fileTree(classKotlinDir) {
            exclude(fileFilter)
        }

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val jacocoReportProvider =
    tasks.register<JacocoReport>("jacocoTestReport") {
        dependsOn("testDebugUnitTest")
        reports {
            csv.required.set(false)
            xml.required.set(true)
            html.required.set(true)
        }

        sourceDirectories.setFrom(files(mainJavaSrc, mainKotlinSrc))
        classDirectories.setFrom(files(debugTree))
        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include(executionPath)
            },
        )
    }

val jacocoCoverageVerificationProvider =
    tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(jacocoReportProvider)

        sourceDirectories.setFrom(files(mainJavaSrc, mainKotlinSrc))
        classDirectories.setFrom(files(debugTree))
        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include(executionPath)
            },
        )

        violationRules {
            isFailOnViolation = true
            rule {
                element = "BUNDLE"
                limit {
                    counter = "INSTRUCTION"
                    minimum = "0.98".toBigDecimal()
                }
                limit {
                    counter = "BRANCH"
                    minimum = "0.95".toBigDecimal()
                }
                limit {
                    counter = "COMPLEXITY"
                    minimum = "0.96".toBigDecimal()
                }
                limit {
                    counter = "LINE"
                    minimum = "0.99".toBigDecimal()
                }
                limit {
                    counter = "METHOD"
                    minimum = "0.98".toBigDecimal()
                }
                limit {
                    counter = "CLASS"
                    minimum = "0.99".toBigDecimal()
                }
            }
        }
    }
