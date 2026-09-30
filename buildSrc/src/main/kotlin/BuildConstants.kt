/**
 * Centralized constant keys used for generating build properties and iOS configurations.
 *
 * These constants form the critical bridge ("contract") between the Gradle build system 
 * (such as [composeApp/build.gradle.kts](file:///home/marcel/MultiPlatformProjects/Projects/PhotoUploader/composeApp/build.gradle.kts)) 
 * and the multiplatform application code.
 *
 * ### How it works across platforms:
 * 1. **Build Generation:** Gradle build scripts use these keys to write build metadata 
 *    (e.g., version name, app id, targets) into generated assets like `build-info.properties` 
 *    or iOS `.xcconfig` configuration files.
 * 2. **Platform Provisioning:** [composeApp/build.gradle.kts](file:///home/marcel/MultiPlatformProjects/Projects/PhotoUploader/composeApp/build.gradle.kts) 
 *    ensures these generated property files are correctly bundled into each platform's resources.
 * 3. **Runtime Consumption:** Platform-specific runtime classes (e.g., `AndroidAppInfo`, `JvmAppInfo`) 
 *    load these properties at startup (mirroring these keys in `AppInfo`) to populate UI screens 
 *    like [AboutScreen.kt](file:///home/marcel/MultiPlatformProjects/Projects/PhotoUploader/composeApp/src/commonMain/kotlin/com/truepineapps/photouploader/core/feature/moremenu/ui/AboutScreen.kt).
 */
@Suppress("unused") // Android Studio cannot detect usages in Gradle build scripts (.gradle.kts)
object BuildConstants {
    // Property Keys (used in build-info.properties)
    const val KEY_APP_ID = "app_id"
    const val KEY_APP_NAME = "app_name"
    const val KEY_APP_LABEL = "app_label"
    const val KEY_APP_MAJOR = "app_major"
    const val KEY_APP_STAGE = "app_stage"
    const val KEY_VERSION_NAME = "version_name"
    const val KEY_TARGET_SDK = "target_sdk"
    const val KEY_JVM_TARGET = "jvm_target"

    // iOS xcconfig Keys
    const val KEY_IOS_APP_NAME = "CFG_APP_NAME"
    const val KEY_IOS_APP_MAJOR = "CFG_APP_MAJOR"
    const val KEY_IOS_APP_STAGE = "CFG_APP_STAGE"

    // Default / Fallback Values
    const val DEFAULT_APP_NAME = "PhotoUploader"
}
