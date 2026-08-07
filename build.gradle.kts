plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinJvm) apply false
}

// Get PlatformVersion object for app name and version based on libs.versions.toml
val versionInfo = extensions.create<ProjectVersion>("versionInfo", project)

tasks.register<SyncIosVersionTask>("syncIosVersion") {
    group = "versioning"
    description = "Syncs the project version to the iOS Config.xcconfig file."
    
    configFile.set(layout.projectDirectory.file("iosApp/Configuration/Config.xcconfig"))
    appName.set(versionInfo.appName)
    appLabel.set(versionInfo.appLabel)
    bundleId.set(versionInfo.appId)
    appMajor.set(versionInfo.appMajor)
    appStage.set(versionInfo.appStage)
    numericVersion.set(versionInfo.numericVersion)
    buildNumber.set(versionInfo.buildNumber)
}
