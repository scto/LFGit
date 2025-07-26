import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
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

    signingConfigs {
        create("general") {
            storeFile = file("test.keystore")
            keyAlias = "test"
            keyPassword = "teixeira0x"
            storePassword = "teixeira0x"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("general")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("general")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
	
	annotationProcessor(libs.androidx.room.compiler)
	
	testImplementation(libs.androidx.room.room-testing)
	
	implementation(libs.compose.preference.library)
	
	
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.swiperefreshlayout)
	implementation(libs.androidx.drawerlayout)
	implementation(libs.androidx.lifecycle.extensions)
	
	implementation(libs.kotlin.stdlib.jdk7)
	
    implementation(libs.commons.io)
	implementation(libs.commons.lang3)
	
    testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.test.ext)
	androidTestImplementation(libs.androidx.test.espresso.core)
	
    coreLibraryDesugaring(libs.androidx.desugar)
}
