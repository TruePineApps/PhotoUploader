package com.truepineapps.photouploader.core.feature.moremenu.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.privacy_policy

object PrivacyPolicyDestination : NavigationDestination {
    override val route = "privacy_policy"
    override val titleRes = Res.string.privacy_policy
}
