import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Plugin that makes this module an Android application
    alias(libs.plugins.androidApplication)
    // KSP
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.truepineapps.photouploader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = libs.versions.appId.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
    }

    sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")

    // Packaging options to resolve duplicate file errors, common in KMP projects
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Or your desired Java version
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true // If you use coreLibraryDesugaring
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    dependencies {
        implementation(projects.composeApp)

        /* Android dependencies */
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.viewmodelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.androidx.datastore.preferences)
        implementation(libs.androidx.preference.ktx)
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.androidx.material3.window.size)
        debugImplementation(libs.androidx.ui.tooling.preview)
        debugImplementation(libs.androidx.ui.tooling)


        // Google Auth dependency
        implementation(libs.play.services.auth)
        // For memory leak detection in debug builds
        debugImplementation(libs.leakcanary.android)
        // For desugaring Java 8+ features
        coreLibraryDesugaring(libs.desugar.jdk.libs)

        // Koin dependency specific to Android
        implementation(libs.koin.android)

        // Handle Koin's KSP configuration for Android
        ksp(libs.koin.ksp.compiler)

        /* Test dependencies */
        testImplementation(kotlin("test"))
        testImplementation(libs.junit)
        testImplementation(libs.mockito.junit.jupiter)

        testImplementation(libs.bundles.shared.commonTest)
        testImplementation(libs.bundles.shared.androidTest)
        // File System
        testImplementation(libs.okio.fakefilesystem)
        // Networking
        testImplementation(libs.ktor.client.mock)
        // Mock
        testImplementation(libs.mockito.junit.jupiter)

        // Robolectric - a simulated Android environment for unit testing
        testImplementation(libs.robolectric)
    }
}
