package com.truepineapps.photouploader.core.feature.settings.domain.model

import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.en
import com.truepineapps.photouploader.resources.nl
import com.truepineapps.photouploader.resources.system
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(
    val code: String,
    val stringRes: StringResource
) {
    System(DEFAULT_LOCALE_FROM_PLATFORM, Res.string.system),
    English("en", Res.string.en),
    Dutch("nl", Res.string.nl)
}