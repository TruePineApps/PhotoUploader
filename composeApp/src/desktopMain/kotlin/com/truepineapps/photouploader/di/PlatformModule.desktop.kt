package com.truepineapps.photouploader.di

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.AppInfo
import com.truepineapps.photouploader.JvmAppInfo
import com.truepineapps.photouploader.JvmPlatformInfo
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.auth.DesktopGoogleAuthService
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.util.DEFAULT_TIMESTAMP_FORMAT
import com.truepineapps.photouploader.util.now
import org.koin.core.module.Module
import org.koin.dsl.module
import java.lang.StringBuilder

object TimestampMessageFormatter : MessageStringFormatter {
    override fun formatMessage(severity: Severity?, tag: Tag?, message: Message): String {
        val timestampString = DEFAULT_TIMESTAMP_FORMAT.format(now())
        // Super implementation prefixed with timestamp
        val sb = StringBuilder(timestampString).append(" ")
        if (severity != null) sb.append(formatSeverity(severity)).append(" ")
        if (tag != null && tag.tag.isNotEmpty()) sb.append(formatTag(tag)).append(" ")
        sb.append(message.message)
        return sb.toString()
    }
}

actual fun platformModule(): Module = module {
    single {
        Logger(
            config = loggerConfigInit(CommonWriter(TimestampMessageFormatter)),
            tag = "PhotoUploader"
        )
    }
    single<GoogleAuthService> { DesktopGoogleAuthService(get()) }
    single<AppInfo> { JvmAppInfo }
    single<PlatformInfo> { JvmPlatformInfo }
}
