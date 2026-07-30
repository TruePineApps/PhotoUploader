package com.truepineapps.photouploader.core.util

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.BuildConfig
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.unknown
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Properties

// Implementation of the shared AppInfo interface for Android
// Note: com.truepineapps.photouploader.BuildConfig is a generated class
object AndroidAppInfo : AppInfo, KoinComponent {
    private val log: Logger by inject()
    private val properties = Properties()

    init {
        try {
            val stream = this::class.java.getResourceAsStream("/build-info.properties")
            if (stream != null) {
                properties.load(stream)
            } else {
                log.e { "AndroidAppInfo: build-info.properties not found in resources" }
            }
        } catch (e: Exception) {
            log.e(e) { "AndroidAppInfo: Error loading build-info.properties" }
        }
    }

    override val appId: String = BuildConfig.APPLICATION_ID
    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: String = BuildConfig.VERSION_CODE.toString()
    override val targetInfo: UiText = run {
        val target = properties.getProperty("target_sdk")
        if (target != null) UiTextString("Target SDK $target")
        else UiTextResource(Res.string.unknown)
    }
}
