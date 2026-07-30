package com.truepineapps.photouploader.core.util

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.unknown
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Properties

// Implementation for Desktop that reads from the generated properties file
object JvmAppInfo : AppInfo, KoinComponent {
    private val log: Logger by inject()
    private val properties = Properties()

    init {
        try {
            val stream = this::class.java.getResourceAsStream("/build-info.properties")
            if (stream != null) {
                properties.load(stream)
            } else {
                log.e { "JvmAppInfo: build-info.properties not found in resources" }
            }
        } catch (e: Exception) {
            log.e(e) { "JvmAppInfo: Error loading build-info.properties" }
        }
    }

    override val appId = properties.getProperty("app_id") ?: "com.truepineapps.photouploader"

    override val versionName = properties.getProperty("version_name") ?: "0.0.1"

    override val versionCode = properties.getProperty("version_code") ?: "0"

    override val targetInfo: UiText = run {
        val target = properties.getProperty("jvm_target")
        if (target != null) UiTextString("JVM $target")
        else UiTextResource(Res.string.unknown)
    }
}
