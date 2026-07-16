package com.truepineapps.photouploader.core.feature.moremenu.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.moremenu.ui.AboutScreen
import com.truepineapps.photouploader.core.feature.moremenu.ui.DebugActionScreen
import com.truepineapps.photouploader.core.feature.moremenu.ui.DocumentType
import com.truepineapps.photouploader.core.feature.moremenu.ui.LegalDocumentScreen
import com.truepineapps.photouploader.core.feature.moremenu.ui.LegalHubScreen
import com.truepineapps.photouploader.core.feature.moremenu.ui.LicenseScreen

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
