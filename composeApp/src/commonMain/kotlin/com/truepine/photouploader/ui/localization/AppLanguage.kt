package com.truepine.photouploader.ui.localization

import com.truepine.photouploader.data.preferences.DEFAULT_LOCALE_FROM_PLATFORM
import com.truepine.photouploader.resources.Res
import com.truepine.photouploader.resources.en
import com.truepine.photouploader.resources.nl
import com.truepine.photouploader.resources.system
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(
    val code: String,
    val stringRes: StringResource
) {
    System(DEFAULT_LOCALE_FROM_PLATFORM, Res.string.system),
    English("en", Res.string.en),
    Dutch("nl", Res.string.nl)
}
