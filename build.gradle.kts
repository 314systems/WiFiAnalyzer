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
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application") version "9.0.1" apply false
    kotlin("plugin.allopen") version "2.3.10" apply false
    id("com.github.ben-manes.versions") version "0.53.0" apply false
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

// Determines if a version string is non-stable (alpha, beta, etc.)
val stableKeywords = listOf("RELEASE", "FINAL", "GA")
val nonStableRegex = Regex("(?i)[.-](alpha|beta|rc|cr|m|preview|b|ea)[.\\d-]*")

fun isNonStable(version: String?): Boolean {
    if (version.isNullOrBlank()) return true // Treat null/empty as non-stable
    val upperVersion = version.uppercase()
    if (stableKeywords.any { upperVersion.contains(it) }) {
        return false
    }
    return nonStableRegex.matches(version)
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    revision = "release" // Only show stable versions in the report
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
