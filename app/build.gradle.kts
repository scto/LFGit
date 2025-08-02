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
    ndkVersion = "28.2.13676358"

    defaultConfig {
        val commit = getGitCommit()
        
        applicationId = "com.lfgit"

        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "GIT_COMMIT", "\"$commit\"")
		
		ndk {
            abiFilters += listOf("arm64-v8a", "x86_64", "armeabi-v7a")
        }
    }
    
	externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
	
	flavorDimensions += "cpuArch"

    productFlavors {
        create("arm8") {
            dimension = "cpuArch"
            isDefault = true
        }
        create("x86_64") {
            dimension = "cpuArch"
        }
		create("universal") {
            dimension = "cpuArch"
			// include all default ABIs. with NDK-r16,  it is:
            //   armeabi-v7a, arm64-v8a, x86, x86_64
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
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.preference)
    
    implementation(libs.google.material)
    
    implementation(libs.kotlin.stdlib.jdk7)
    
    implementation(libs.apache.commons.io)
    implementation(libs.apache.commons.lang3)
    
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.guava)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.runtime)
    //ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.junit)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    coreLibraryDesugaring(libs.androidx.desugar)
}

fun getGitCommit(): String {
    return try {
        val commit = providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
        println("Git commit: $commit")
        commit
    } catch (_: Exception) {
        ""
    }
}