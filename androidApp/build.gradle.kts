import org.gradle.api.JavaVersion

plugins {
    // Plugin that makes it an Android application module
    alias(libs.plugins.androidApplication)
    // KSP
    alias(libs.plugins.ksp)
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

dependencies {
    /* Core Android dependencies */
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.preference.ktx)

    // Google Auth dependency
    implementation(libs.play.services.auth)
    // For memory leak detection in debug builds
    debugImplementation(libs.leakcanary.android)
    // For desugaring Java 8+ features
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    /* Compose dependencies specific to Android */
    // For @Preview annotations
    implementation(libs.ui.tooling.preview)
    // For UI inspection in debug builds
    debugImplementation(libs.ui.tooling)

    // Koin dependency specific to Android
    implementation(libs.koin.android)

    // Networking dependencies specific to Android
    implementation(libs.ktor.client.android)
    implementation(libs.kotlinx.coroutines.android)

    // Handle Koin's KSP configuration for Android
    ksp(libs.koin.ksp.compiler)

    // Dependency on the shared KMP module , which is called 'composeApp'
    implementation(project(":composeApp"))

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

    // Preferences
    testImplementation(libs.androidx.datastore.preferences)
    testImplementation(libs.androidx.preference.ktx)
    // ViewModel
    testImplementation(libs.androidx.lifecycle.viewmodelCompose)

}
