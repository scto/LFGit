import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    //alias(libs.plugins.room)
    //alias(libs.plugins.kotlin.detekt)
}

android {
    namespace = "com.lfgit"
    ndkVersion = "28.0.13004108"

    defaultConfig {
        applicationId = "com.lfgit"

        vectorDrawables.useSupportLibrary = true

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64", "armeabi-v7a")
        }

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    /*
    room {
        // The schemas directory contains a schema file for each version of the Room database.
        // This is required to enable Room auto migrations.
        // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
        //schemaDirectory "$projectDir/schemas"
        schemaDirectory("$projectDir/schemas")
        generateKotlin = true
    }
    */

    /*
    signingConfigs {
        create("general") {
            storeFile = file("test.keystore")
            keyAlias = "test"
            keyPassword = "teixeira0x"
            storePassword = "teixeira0x"
        }
    }
    */

    buildTypes {
        release {
            isMinifyEnabled = false
            //signingConfig = signingConfigs.getByName("general")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        /*
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("general")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        */
    }

    compileOptions { isCoreLibraryDesugaringEnabled = true }

    packaging {
        resources.excludes.addAll(
            arrayOf(
                "META-INF/README.md",
                "META-INF/CHANGES",
                "bundle.properties",
                "plugin.properties"
            )
        )

        jniLibs { useLegacyPackaging = true }
    }

    lint {
        abortOnError = false
        disable += listOf("MaterialDesignInsteadOrbitDesign")
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
        compose = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.guava)
    implementation(libs.androidx.room.rxjava2)

    implementation(libs.androidx.room.runtime)
    //ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    
    testImplementation(libs.androidx.room.testing)

    implementation(libs.compose.preference.library)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.lifecycle.extensions)

    implementation(libs.kotlin.stdlib.jdk7)

    implementation(libs.apache.commons.io)
    implementation(libs.apache.commons.lang3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    coreLibraryDesugaring(libs.androidx.desugar)
}
