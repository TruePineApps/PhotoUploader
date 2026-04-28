package com.truepineapps.photouploader.core.feature.settings.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.settings

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = Res.string.settings
}