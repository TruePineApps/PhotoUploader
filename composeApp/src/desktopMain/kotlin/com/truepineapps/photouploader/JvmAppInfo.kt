package com.truepineapps.photouploader

import java.util.Properties

// Implementation for Desktop that reads from the generated properties file
object JvmAppInfo : AppInfo {
    private val properties = Properties()

    init {
        // Load the file we generated in Gradle
        val stream = this::class.java.getResourceAsStream("/build-info.properties")
        if (stream != null) {
            properties.load(stream)
        }
    }

    override val appId: String
        get() = properties.getProperty("app_id") ?: "com.truepineapps.photouploader"

    override val versionName: String
        get() = properties.getProperty("version_name") ?: "0.0.1"

    override val versionCode: String
        get() = properties.getProperty("version_code") ?: "0"
}