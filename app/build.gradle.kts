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
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("plugin.allopen")
    jacoco
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

apply(
    from = "jacoco.gradle.kts",
)

// dependencies -------------------------------------------------
dependencies {
    // Compile Build Dependencies
    implementation(fileTree("libs") { include("*.jar") })
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.collection:collection-ktx:1.5.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.media:media:1.7.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.jjoe64:graphview:4.2.2")
    // Unit Test Dependencies
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("com.googlecode.junit-toolbox:junit-toolbox:2.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.2.3")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.hamcrest:hamcrest:3.0")
    // Android Test Dependencies
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("org.assertj:assertj-core:3.27.7")
    androidTestImplementation("org.hamcrest:hamcrest:3.0")
}

android {
    namespace = "com.vrem.wifianalyzer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vrem.wifianalyzer"
        minSdk = 24
        targetSdk = 36
        versionCode
        versionName
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
    if (isReleaseTask()) {
        val keystorePropertiesFile = rootProject.file("androidkeystore.properties")
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))

        android {
            signingConfigs {
                create("releaseConfig") {
                    keyAlias = keystoreProperties["key_alias"] as String
                    keyPassword = keystoreProperties["key_password"] as String
                    storeFile = file(keystoreProperties["store_filename"] as String)
                    storePassword = keystoreProperties["store_password"] as String
                }
            }
        }
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
    val finalAppId =
        if (isRelease) {
            android.defaultConfig.applicationId
        } else {
            "${android.defaultConfig.applicationId}${android.buildTypes.getByName("debug").applicationIdSuffix}"
        }

    println(">>> ${project.name} $finalVersionName ($versionStore) $finalAppId")

    android.defaultConfig.apply {
        versionCode = versionStore
        versionName = finalVersionName
    }
}

fun isTestTask() = gradle.startParameter.taskNames.any { it.contains("UnitTest", ignoreCase = true) }

fun isReleaseTask() = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

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

configurations.all {
    exclude(group = "org.hamcrest", module = "hamcrest-core")
    exclude(group = "org.hamcrest", module = "hamcrest-library")
}
