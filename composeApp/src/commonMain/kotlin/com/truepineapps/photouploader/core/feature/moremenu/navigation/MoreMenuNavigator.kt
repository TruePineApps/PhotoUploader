/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.feature.moremenu.navigation

import androidx.navigation.NavHostController
import com.truepineapps.photouploader.core.feature.settings.navigation.SettingsDestination

interface MoreMenuNavigator {
    fun navigateToSettings()
    fun navigateToAbout()
    fun navigateToLegalHub()
    fun navigateToLicenseScreen()
    fun navigateToDebugActions()
}

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