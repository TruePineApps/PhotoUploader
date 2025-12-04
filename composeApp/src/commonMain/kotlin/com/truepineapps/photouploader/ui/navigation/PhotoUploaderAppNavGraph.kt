/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.truepineapps.photouploader.ui.screen.about.AboutDestination
import com.truepineapps.photouploader.ui.screen.about.AboutScreen
import com.truepineapps.photouploader.ui.screen.about.LicenseDestination
import com.truepineapps.photouploader.ui.screen.about.LicenseScreen
import com.truepineapps.photouploader.ui.screen.settings.SettingsDestination
import com.truepineapps.photouploader.ui.screen.settings.SettingsScreen
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderDestination
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderScreen
import org.koin.compose.koinInject

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun PhotoUploaderAppNavHost(
    navController: NavHostController,
    isHorizontalLayout: Boolean,
    onUpdateTopAppBar: (title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit,
    showDirPicker: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = PhotoUploaderDestination.route,
) {
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        /* Photo Uploader Screens */
        composable(route = PhotoUploaderDestination.route) {
            PhotoUploaderScreen(
                isHorizontalLayout = isHorizontalLayout,
                onUpdateTopAppBar = onUpdateTopAppBar,
                showDirPicker = showDirPicker,
                navigateToPhotos = { path -> navController.navigate("${PhotoUploaderDestination.route}/${path}") },
                modifier = Modifier.fillMaxSize(),
            )
        }

        /* Menu screens */
        composable(route = SettingsDestination.route) {
            SettingsScreen(
                onUpdateTopAppBar = onUpdateTopAppBar,
                modifier = Modifier.fillMaxSize(),
                settingsViewModel = koinInject()
            )
        }
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
}
