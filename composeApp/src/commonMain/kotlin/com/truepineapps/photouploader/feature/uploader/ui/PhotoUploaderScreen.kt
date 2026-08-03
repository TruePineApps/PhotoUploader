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

package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.presentation.component.LoadingScreen
import com.truepineapps.photouploader.core.presentation.component.ThemedIconButton
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.core.util.isExpandedWidth
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UiState
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.app_name
import com.truepineapps.photouploader.resources.photo_uploader
import com.truepineapps.photouploader.resources.select_photo_folder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object PhotoUploaderDestination : NavigationDestination {
    override val route = "photo_uploader"
    override val titleRes = Res.string.photo_uploader
}

@Composable
fun PhotoUploaderScreen(
    onUpdateTopAppBar: (title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit,
    showDirPicker: () -> Unit,
    navigateToPhotos: (String) -> Unit,
    viewModel: PhotoUploaderViewModel,
    modifier: Modifier = Modifier,
    log: Logger = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appName = stringResource(Res.string.app_name)

    // Update TopAppBar title when path changes or on initial composition
    LaunchedEffect(uiState.path) {
        val title = uiState.path.ifBlank { appName }
        onUpdateTopAppBar(title, null) {}
    }

    LoadingScreen(loadingViewModel = viewModel, log = log, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.albumUiStates.isEmpty() && uiState.path.isEmpty()) {
                StartScreen(
                    showDirPicker = showDirPicker,
                    canChooseDirectory = !uiState.isUploading,
                    modifier = modifier.fillMaxSize()
                )
            } else if (uiState.albumUiStates.isNotEmpty()) {
                BoxWithConstraints {
                    FolderContent(
                        uiState = uiState,
                        isHorizontalLayout = isExpandedWidth(maxWidth),
                        navigateToPhotos = navigateToPhotos,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun FolderContent(
    uiState: UiState,
    isHorizontalLayout: Boolean,
    navigateToPhotos: (String) -> Unit,
    viewModel: PhotoUploaderViewModel,
    modifier: Modifier = Modifier
){
    val albumIdToShow = uiState.selectedAlbumId ?: uiState.albumUiStates.first().id
    if (isHorizontalLayout) {
        Logger.d("FolderContent: Horizontal layout")
        Row(
            modifier = modifier
                .padding(Dimensions.padding_small)
        ) {
            Box(modifier = Modifier.weight(0.5f)) {
                AlbumListContent(
                    groupUiStates = uiState.groupUiStates,
                    selectedAlbumId = albumIdToShow,
                    isUploading = uiState.isUploading,
                    onAlbumClick = { album -> viewModel.updateSelectedAlbum(album.id) },
                    onAlbumToggle = viewModel::toggleAlbum,
                    onAlbumRename = viewModel::renameAlbum,
                    onAlbumGroupToggle = viewModel::toggleGroup,
                    onAlbumGroupExpanded = viewModel::toggleGroupExpanded,
                )
            }
            Box(modifier = Modifier.weight(0.5f)) {
                PhotoListScreen(
                    albumId = albumIdToShow,
                    onUpdateTopAppBar = null,
                    onBackClick = { },
                    viewModel = viewModel,
                )
            }
        }
    } else {
        Logger.d("FolderContent: Compact layout")
        AlbumListContent(
            groupUiStates = uiState.groupUiStates,
            selectedAlbumId = albumIdToShow,
            isUploading = uiState.isUploading,
            onAlbumClick = { album ->
                viewModel.updateSelectedAlbum(album.id)
                navigateToPhotos(album.id)
            },
            onAlbumToggle = viewModel::toggleAlbum,
            onAlbumRename = viewModel::renameAlbum,
            onAlbumGroupToggle = viewModel::toggleGroup,
            onAlbumGroupExpanded = viewModel::toggleGroupExpanded,
            modifier = modifier
        )
    }
}

@Composable
fun StartScreen(showDirPicker: () -> Unit, canChooseDirectory: Boolean, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        ThemedIconButton(
            imageVector = Icons.Filled.PermMedia,
            contentDescriptionResource = Res.string.select_photo_folder,
            iconSize = Dimensions.big_icon_size,
            enabled = canChooseDirectory,
            onClick = showDirPicker,
        )
        Text(
            text = stringResource(Res.string.select_photo_folder),
            modifier = Modifier.padding(Dimensions.padding_medium)
        )
    }
}
