package com.truepineapps.photouploader.core.feature.settings.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.settings.ui.SettingsScreen
import com.truepineapps.photouploader.core.feature.settings.viewmodel.SettingsViewModel

fun NavGraphBuilder.settingsGraph(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    onNavigateBack: () -> Unit,
    log: Logger,
    settingsViewModel: SettingsViewModel
) {
    composable(route = SettingsDestination.route) {
        SettingsScreen(
            onUpdateTopAppBar = onUpdateTopAppBar,
            onNavigateBack = onNavigateBack,
            log = log,
            settingsViewModel = settingsViewModel,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
