package com.truepineapps.photouploader.core.feature.about.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.truepineapps.photouploader.core.feature.about.ui.AboutScreen
import com.truepineapps.photouploader.core.feature.about.ui.LicenseScreen

fun NavGraphBuilder.aboutGraph(onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit) {
    composable(route = AboutDestination.route) {
        AboutScreen(
            onUpdateTopAppBar = onUpdateTopAppBar, modifier = Modifier.fillMaxSize()
        )
    }
    composable(route = LicenseDestination.route) {
        LicenseScreen(
            onUpdateTopAppBar = onUpdateTopAppBar, modifier = Modifier.fillMaxSize()
        )
    }
}