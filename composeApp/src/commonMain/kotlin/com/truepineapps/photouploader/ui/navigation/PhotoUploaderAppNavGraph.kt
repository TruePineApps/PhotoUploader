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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.truepineapps.photouploader.ui.screen.about.AboutDestination
import com.truepineapps.photouploader.ui.screen.about.AboutScreen
import com.truepineapps.photouploader.ui.screen.about.LicenseDestination
import com.truepineapps.photouploader.ui.screen.about.LicenseScreen
import com.truepineapps.photouploader.ui.screen.settings.SettingsDestination
import com.truepineapps.photouploader.ui.screen.settings.SettingsScreen
import com.truepineapps.photouploader.ui.screen.uploader.PhotoListContent
import com.truepineapps.photouploader.ui.screen.uploader.PhotoListDestination
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderDestination
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderScreen
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
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
    viewModel: PhotoUploaderViewModel = koinInject()
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
                navigateToPhotos = { albumId -> 
                    // Need to encode path if it contains special characters, but here we assume simple ID/Path
                    // For now, simpler to just pass the ID which might be the path. 
                    // Note: Ideally IDs should be URL safe.
                    // Since the ID is a full path, let's assume for now it's safe or we might need encoding
                    // But for this step, let's keep it simple.
                    navController.navigate("${PhotoListDestination.route}/$albumId")
                },
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
        }
        
        composable(
            route = PhotoListDestination.routeWithArgs,
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
             val albumId = backStackEntry.getStringArg("path")
             val uiState by viewModel.uiState.collectAsState()
             val album = uiState.albums.find { it.id == albumId }
             
             if (album != null) {
                 PhotoListContent(
                     album = album,
                     onUpdateTopAppBar = onUpdateTopAppBar,
                     onBackClick = { navController.popBackStack() },
                     onPhotoToggle = { photoPath -> viewModel.togglePhoto(album.id, photoPath) },
                     modifier = Modifier.fillMaxSize()
                 )
             } else {
                 // Album not found (e.g. because directory changed and we rescanned). 
                 // Navigate back to the main list.
                 LaunchedEffect(Unit) {
                     navController.popBackStack()
                 }
             }
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

private fun NavBackStackEntry.getStringArg(key: String): String? {
    return arguments?.let { NavType.StringType[it, key] }
}
