package com.truepineapps.photouploader.core.navigation

import androidx.navigation.NavHostController
import com.truepineapps.photouploader.core.feature.legal.navigation.LegalDestination
import com.truepineapps.photouploader.core.feature.legal.navigation.LicenseDestination
import com.truepineapps.photouploader.core.feature.moremenu.navigation.AboutDestination
import com.truepineapps.photouploader.core.feature.moremenu.navigation.DebugActionDestination
import com.truepineapps.photouploader.core.feature.moremenu.navigation.MoreMenuNavigator
import com.truepineapps.photouploader.core.feature.settings.navigation.SettingsDestination

class MoreMenuNavigatorImpl(private val navController: NavHostController) : MoreMenuNavigator {
    override fun navigateToSettings() {
        navController.navigate(SettingsDestination.route)
    }

    override fun navigateToAbout() {
        navController.navigate(AboutDestination.route)
    }

    override fun navigateToLegalHub() {
        navController.navigate(LegalDestination.route)
    }

    override fun navigateToLicenseScreen() {
        navController.navigate(LicenseDestination.route)
    }

    override fun navigateToDebugActions() {
        navController.navigate(DebugActionDestination.route)
    }
}