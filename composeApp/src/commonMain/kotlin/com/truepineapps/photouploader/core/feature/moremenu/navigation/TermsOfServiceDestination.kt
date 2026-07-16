package com.truepineapps.photouploader.core.feature.moremenu.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.terms_of_service

object TermsOfServiceDestination : NavigationDestination {
    override val route = "terms_of_service"
    override val titleRes = Res.string.terms_of_service
}
