package com.truepineapps.photouploader.core.feature.about.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.about

object AboutDestination : NavigationDestination {
    override val route = "about"

    override val titleRes = Res.string.about
}