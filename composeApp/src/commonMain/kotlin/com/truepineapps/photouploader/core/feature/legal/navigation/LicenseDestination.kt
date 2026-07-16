package com.truepineapps.photouploader.core.feature.legal.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.licenses

object LicenseDestination : NavigationDestination {
    override val route = "license"
    override val titleRes = Res.string.licenses
}