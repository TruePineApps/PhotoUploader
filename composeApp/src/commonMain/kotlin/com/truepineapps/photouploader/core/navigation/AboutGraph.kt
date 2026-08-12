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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.legal.navigation.LegalDestination
import com.truepineapps.photouploader.core.feature.legal.navigation.LicenseDestination
import com.truepineapps.photouploader.core.feature.legal.navigation.PrivacyPolicyDestination
import com.truepineapps.photouploader.core.feature.legal.navigation.TermsOfServiceDestination
import com.truepineapps.photouploader.core.feature.legal.ui.DocumentType
import com.truepineapps.photouploader.core.feature.legal.ui.LegalDocumentScreen
import com.truepineapps.photouploader.core.feature.legal.ui.LegalHubScreen
import com.truepineapps.photouploader.core.feature.legal.ui.LicenseScreen
import com.truepineapps.photouploader.core.feature.moremenu.navigation.AboutDestination
import com.truepineapps.photouploader.core.feature.moremenu.navigation.DebugActionDestination
import com.truepineapps.photouploader.core.feature.moremenu.navigation.PrivacyDestination
import com.truepineapps.photouploader.core.feature.moremenu.ui.AboutScreen
import com.truepineapps.photouploader.core.feature.moremenu.ui.DebugActionScreen

fun NavGraphBuilder.aboutGraph(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    navController: NavHostController,
    log: Logger
) {
    composable(route = AboutDestination.route) {
        AboutScreen(
            onUpdateTopAppBar = onUpdateTopAppBar,
            scrollToAccountPrivacy = false,
            modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = PrivacyDestination.route) {
        log.d("Navigate to Privacy")
        AboutScreen(
            onUpdateTopAppBar = onUpdateTopAppBar,
            scrollToAccountPrivacy = true,
            modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = DebugActionDestination.route) {
        DebugActionScreen(modifier = Modifier.fillMaxSize())
    }
    composable(route = LicenseDestination.route) {
        LicenseScreen(onUpdateTopAppBar = onUpdateTopAppBar, modifier = Modifier.fillMaxSize())
    }
    composable(route = LegalDestination.route) {
        LegalHubScreen(
            onNavigateToLicense = { navController.navigate(LicenseDestination.route) },
            onNavigateToTerms = { navController.navigate(TermsOfServiceDestination.route) },
            onNavigateToPrivacy = { navController.navigate(PrivacyPolicyDestination.route) },
            onNavigateToManageData = { navController.navigate(PrivacyDestination.route) },
            onUpdateTopAppBar = onUpdateTopAppBar,
            modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = TermsOfServiceDestination.route) {
        LegalDocumentScreen(
            type = DocumentType.TERMS,
            onUpdateTopAppBar = onUpdateTopAppBar,
            log = log,
            modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = PrivacyPolicyDestination.route) {
        LegalDocumentScreen(
            type = DocumentType.PRIVACY,
            onUpdateTopAppBar = onUpdateTopAppBar,
            log = log,
            modifier = Modifier.fillMaxSize()
        )
    }
}
