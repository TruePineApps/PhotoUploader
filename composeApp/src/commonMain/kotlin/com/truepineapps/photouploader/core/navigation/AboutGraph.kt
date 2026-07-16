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
            modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = DebugActionDestination.route) {
        DebugActionScreen(modifier = Modifier.fillMaxSize())
    }
    composable(route = LicenseDestination.route) {
        LicenseScreen(
            onUpdateTopAppBar = onUpdateTopAppBar, modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = LegalDestination.route) {
        LegalHubScreen(
            onNavigateToLicense = { navController.navigate(LicenseDestination.route) },
            onNavigateToTerms = { navController.navigate(TermsOfServiceDestination.route) },
            onNavigateToPrivacy = { navController.navigate(PrivacyPolicyDestination.route) },
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
