import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.2"

    defaultConfig {
        applicationId = "com.lfgit"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 18
        versionName = "1.122"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            ndkBuild {
                cFlags.addAll(
                    listOf(
                        "-std=c11",
                        "-Wall",
                        "-Wextra",
                        "-Werror",
                        "-Os",
                        "-fno-stack-protector",
                        "-Wl,--gc-sections"
                    )
                [cite_start]) [cite: 2]
            }
        }

        ndk {
            abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
        }
    }

    buildTypes {
        getByName("release") {
            [cite_start]isMinifyEnabled = false [cite: 3]
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    dataBinding {
        isEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    externalNativeBuild {
        ndkBuild {
            [cite_start]path = file("src/main/cpp/Android.mk") [cite: 4]
        }
    }

    bundle {
        language {
            enableSplit.set(false)
        }
        density {
            enableSplit.set(false)
        }
        abi {
            [cite_start]enableSplit.set(true) [cite: 5]
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val roomVersion = "2.2.5"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.preference:preference:1.1.1")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    // For Kotlin use kapt instead of annotationProcessor
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    // optional - RxJava support for Room
    implementation("androidx.room:room-rxjava2:$roomVersion")
    [cite_start]// optional - Guava support for Room, including Optional and ListenableFuture [cite: 6]
    [cite_start]implementation("androidx.room:room-guava:$roomVersion") [cite: 6]
    // Test helpers
    testImplementation("androidx.room:room-testing:$roomVersion")

    implementation(group = "commons-io", name = "commons-io", version = "20030203.000550")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("org.apache.commons:commons-lang3:3.11")
}