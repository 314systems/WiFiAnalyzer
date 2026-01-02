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
    id("kotlin-android")
    id("kotlin-allopen")
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

apply (
    from = "jacoco.gradle"
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
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.media:media:1.7.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    implementation("com.jjoe64:graphview:4.2.2")
    // Unit Test Dependencies
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("com.googlecode.junit-toolbox:junit-toolbox:2.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.1.0")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.2.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.2.21")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("org.assertj:assertj-core:3.27.6")
    // Android Test Dependencies
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("org.assertj:assertj-core:3.27.6")
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
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
    kotlinOptions {
        jvmTarget = "17"
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
        val propertiesFile = file("androidkeystore.properties")
        if (propertiesFile.exists()) {
            val properties = readProperties(propertiesFile)
            println(">>> Signing Config " + properties)
            android.signingConfigs.create("releaseConfig") {
                keyAlias = properties["key_alias"].toString()
                keyPassword = properties["key_password"].toString()
                storeFile = file(properties["store_filename"].toString())
                storePassword = properties["store_password"].toString()
            }
            android.buildTypes.getByName("release").signingConfig = android.signingConfigs.getByName("releaseConfig")
        } else {
            System.err.println(">>> No Signing Config found! Missing '" + propertiesFile.name + "' file!")
        }
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
        versionBuild = 0
        properties["version_patch"] = versionPatch.toString()
        properties["version_store"] = versionStore.toString()
        properties["version_build"] = versionBuild.toString()
        writeProperties(propertiesFile, properties)
    }
    if (isTestTask()) {
        println(">>> Running Tests...")
        versionBuild++
        properties["version_build"] = versionBuild.toString()
        writeProperties(propertiesFile, properties)
    }

    var versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
    var applicationId = android.defaultConfig.applicationId
    if (!isReleaseTask()) {
        versionName = versionName + ".${versionBuild}"
        applicationId = applicationId + android.buildTypes.get("debug").applicationIdSuffix
    }
    println(">>> " + project.parent?.name + " " + versionName + " (" + versionStore + ") " + applicationId)
    android.defaultConfig.versionCode = versionStore
    android.defaultConfig.versionName = versionName
}

fun isTestTask(): Boolean {
    val tasks = gradle.startParameter.taskNames
    return ":app:testDebugUnitTest" in tasks || "testDebugUnitTest" in tasks ||
            ":app:testReleaseUnitTest" in tasks || "testReleaseUnitTest" in tasks
}

fun isReleaseTask(): Boolean {
    val tasks = gradle.startParameter.taskNames
    return ":app:assembleRelease" in tasks || "assembleRelease" in tasks ||
            ":app:bundleRelease" in tasks || "bundleRelease" in tasks
}

fun readProperties(propertiesFile: File): Properties {
    if (propertiesFile.canRead()) {
        val properties = Properties()
        val inputStream = FileInputStream(propertiesFile)
        properties.load(inputStream)
        inputStream.close()
        return properties
    } else {
        val message = ">>> Could not read " + propertiesFile.name + " file!"
        System.err.println(message)
        throw RuntimeException(message)
    }
}

fun writeProperties(propertiesFile: File, properties: Properties) {
    val writer = propertiesFile.writer()
    properties.store(writer, "Build Properties")
    writer.close()
}
