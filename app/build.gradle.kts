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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.allopen)
    jacoco
    alias(libs.plugins.ktlint)
}

apply(
    from = "jacoco.gradle",
)

// dependencies -------------------------------------------------
dependencies {
    // Compile Build Dependencies
    implementation(fileTree("libs") { include("*.jar") })
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.media)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.graphview)
    // Unit Test Dependencies
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit.toolbox)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.assertj.core)
    // Android Test Dependencies
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.assertj.core)
}

android {
    namespace = "com.vrem.wifianalyzer"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

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
            signingConfig
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
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-XX:+EnableDynamicAgentLoading")
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    it.outputs.upToDateWhen { false }
                    showStandardStreams = true
                }
            }
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
    val propertiesFile = file("androidkeystore.properties")

    if (propertiesFile.exists()) {
        val properties = readProperties(propertiesFile)

        android {
            signingConfigs.apply {
                create("releaseConfig") {
                    keyAlias = properties.getProperty("key_alias")
                    keyPassword = properties.getProperty("key_password")
                    storePassword = properties.getProperty("store_password")
                    properties.getProperty("store_filename")?.let {
                        storeFile = file(it)
                    }
                }
            }

            buildTypes.getByName("release") {
                signingConfig = android.signingConfigs.getByName("releaseConfig")
            }
        }

        if (isReleaseTask()) {
            println(">>> Release signing configured using ${propertiesFile.name}")
        }
    } else if (isReleaseTask()) {
        logger.error(">>> No Signing Config found! Missing '${propertiesFile.name}' file!")
    }
}

// version -------------------------------------------------
updateVersion()

fun updateVersion() {
    val propertiesFile = file("build.properties")
    if (!propertiesFile.exists()) return
    val properties = readProperties(propertiesFile)

    fun getIntProp(key: String) = properties.getProperty(key)?.toInt() ?: 0

    val versionMajor = getIntProp("version_major")
    val versionMinor = getIntProp("version_minor")
    var versionPatch = getIntProp("version_patch")
    var versionBuild = getIntProp("version_build")
    var versionStore = getIntProp("version_store")

    val isRelease = isReleaseTask()
    val isTest = isTestTask()

    if (isRelease) {
        println(">>> Building Release...")
        versionPatch++
        versionStore++
        versionBuild = 0
    } else if (isTest) {
        println(">>> Running Tests...")
        versionBuild++
    }

    if (isRelease || isTest) {
        properties["version_patch"] = versionPatch.toString()
        properties["version_store"] = versionStore.toString()
        properties["version_build"] = versionBuild.toString()
        writeProperties(propertiesFile, properties)
    }

    val baseVersion = "$versionMajor.$versionMinor.$versionPatch"
    val finalVersionName = if (isRelease) baseVersion else "$baseVersion.$versionBuild"

    val appId = android.defaultConfig.applicationId
    val suffix = android.buildTypes.getByName("debug").applicationIdSuffix ?: ""
    val finalAppId = if (isRelease) appId else "$appId$suffix"
    println(">>> ${project.name} $finalVersionName ($versionStore) $finalAppId")

    android.defaultConfig.apply {
        versionCode = versionStore
        versionName = finalVersionName
    }
    android.defaultConfig.versionName = finalVersionName
}

fun isTestTask(): Boolean {
    val tasks = gradle.startParameter.taskNames
    return tasks.any { it.contains("UnitTest", ignoreCase = true) }
}

fun isReleaseTask(): Boolean {
    val tasks = gradle.startParameter.taskNames
    return tasks.any { it.contains("Release", ignoreCase = true) }
}

fun readProperties(propertiesFile: File): Properties {
    if (!propertiesFile.canRead()) {
        val message = ">>> Could not read ${propertiesFile.name} file!"
        throw RuntimeException(message)
    }

    return Properties().apply {
        propertiesFile.inputStream().use(::load)
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
