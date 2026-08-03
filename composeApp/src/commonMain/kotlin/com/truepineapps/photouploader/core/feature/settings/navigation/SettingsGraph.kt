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
