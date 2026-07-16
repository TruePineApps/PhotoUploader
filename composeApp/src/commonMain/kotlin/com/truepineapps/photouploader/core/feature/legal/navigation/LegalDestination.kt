package com.truepineapps.photouploader.core.feature.legal.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.legal

object LegalDestination : NavigationDestination {
    override val route = "legal_hub"
    override val titleRes = Res.string.legal
}
