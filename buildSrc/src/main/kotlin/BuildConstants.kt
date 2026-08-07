/**
 * Centralized constant keys used for generating build properties and iOS configurations.
 * 
 * These constants form the "contract" between the Gradle build system and the 
 * application code. In the application, they are defined in AppInfo.
 */
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
