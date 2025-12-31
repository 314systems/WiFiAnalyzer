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
import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.ktlint)
    id("jacoco")
}

apply(from = "jacoco.gradle")

// dependencies -------------------------------------------------
dependencies {
    // Compile Build Dependencies
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.media)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.material)
    implementation(libs.graphview)
    // Unit Test Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit.toolbox)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.assertj.core)
    // Android Test Dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.assertj.core)
}

android {
    namespace = "com.vrem.wifianalyzer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vrem.wifianalyzer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".BETA"
            versionNameSuffix = "-BETA"
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true

        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
            it.testLogging {
                events("passed", "skipped", "failed", "standardOut", "standardError")
                showStandardStreams = true
            }
            it.outputs.upToDateWhen { false }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    lint {
        lintConfig = file("lint.xml")
    }
}

allOpen {
    annotation("com.vrem.annotation.OpenClass")
}

// keystore -------------------------------------------------
signingConfig()

fun signingConfig() {
    if (!isReleaseTask()) return

    val propertiesFile = file("androidkeystore.properties")
    if (propertiesFile.exists()) {
        val properties = readProperties(propertiesFile)
        println(">>> Signing Config $properties")

        val androidExt = extensions.getByType(ApplicationExtension::class.java)

        val releaseConfig =
            androidExt.signingConfigs.create("releaseConfig") {
                keyAlias = properties["key_alias"].toString()
                keyPassword = properties["key_password"].toString()
                storeFile = file(properties["store_filename"].toString())
                storePassword = properties["store_password"].toString()
            }
        androidExt.buildTypes.getByName("release").signingConfig = releaseConfig
    } else {
        System.err.println(">>> No Signing Config found! Missing '${propertiesFile.name}' file!")
    }
}

// version -------------------------------------------------
updateVersion()

fun updateVersion() {
    val propertiesFile = file("build.properties")
    val properties = readProperties(propertiesFile)

    val versionMajor = properties["version_major"].toString().toInt()
    val versionMinor = properties["version_minor"].toString().toInt()
    var versionPatch = properties["version_patch"].toString().toInt()
    var versionBuild = properties["version_build"].toString().toInt()
    var versionStore = properties["version_store"].toString().toInt()

    if (isReleaseTask()) {
        println(">>> Building Release...")
        versionPatch++
        versionStore++
        properties["version_patch"] = versionPatch.toString()
        properties["version_store"] = versionStore.toString()
        properties["version_build"] = "0"
        writeProperties(propertiesFile, properties)
    }

    if (isTestTask()) {
        println(">>> Running Tests...")
        versionBuild++
        properties["version_build"] = versionBuild.toString()
        writeProperties(propertiesFile, properties)
    }

    val androidExt = extensions.getByType(ApplicationExtension::class.java)

    var versionName = "$versionMajor.$versionMinor.$versionPatch"
    var applicationId = androidExt.defaultConfig.applicationId ?: "com.vrem.wifianalyzer"

    if (!isReleaseTask()) {
        val debugSuffix =
            androidExt.buildTypes
                .getByName("debug")
                .applicationIdSuffix
                .orEmpty()
        versionName += ".$versionBuild"
        applicationId += debugSuffix
    }

    println(">>> ${project.parent?.name} $versionName ($versionStore) $applicationId")

    androidExt.defaultConfig.versionCode = versionStore
    androidExt.defaultConfig.versionName = versionName
}

fun Project.isTestTask(): Boolean =
    gradle.startParameter.taskNames.any { task ->
        task.split(":").last().matches(Regex(".*Test$"))
    }

fun Project.isReleaseTask(): Boolean =
    gradle.startParameter.taskNames.any { task ->
        task.split(":").last().matches(Regex(".*Release$"))
    }

fun readProperties(propertiesFile: File): Properties {
    if (!propertiesFile.canRead()) {
        val message = ">>> Could not read ${propertiesFile.name} file!"
        System.err.println(message)
        throw RuntimeException(message)
    }
    return Properties().apply {
        FileInputStream(propertiesFile).use { load(it) }
    }
}

fun writeProperties(
    propertiesFile: File,
    properties: Properties,
) {
    propertiesFile.writer().use { writer ->
        properties.store(writer, "Build Properties")
    }
}
