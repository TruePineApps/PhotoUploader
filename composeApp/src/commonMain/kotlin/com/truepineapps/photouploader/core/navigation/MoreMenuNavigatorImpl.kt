/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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