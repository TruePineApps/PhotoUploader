/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.feature.moreMenu.navigation

import androidx.navigation.NavHostController
import com.truepineapps.photouploader.core.feature.about.navigation.AboutDestination
import com.truepineapps.photouploader.core.feature.about.navigation.LicenseDestination
import com.truepineapps.photouploader.core.feature.settings.navigation.SettingsDestination

interface MoreMenuNavigator {
    fun navigateToSettings()
    fun navigateToAbout()
    fun navigateToLicenseScreen()
}

class MoreMenuNavigatorImpl(private val navController: NavHostController) : MoreMenuNavigator {
    override fun navigateToSettings() {
        navController.navigate(SettingsDestination.route)
    }

    override fun navigateToAbout() {
        navController.navigate(AboutDestination.route)
    }

    override fun navigateToLicenseScreen() {
        navController.navigate(LicenseDestination.route)
    }
}