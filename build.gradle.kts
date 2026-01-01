/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2025 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
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
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    id("com.github.ben-manes.versions") version "0.53.0"
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

fun isNonStable(version: String): Boolean {
    val stableKeywords = listOf("RELEASE", "FINAL", "GA")
    val upperVersion = version.uppercase()
    val hasStableKeyword = stableKeywords.any { upperVersion.contains(it) }
    val nonStableRegex = "^.*[\\.\\-\\_](?:alpha|beta|rc|cr|m|preview|b|ea)[\\.\\d\\-\\_]*$".toRegex(RegexOption.IGNORE_CASE)
    return if (hasStableKeyword) false else nonStableRegex.matches(version)
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    revision = "release"
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
