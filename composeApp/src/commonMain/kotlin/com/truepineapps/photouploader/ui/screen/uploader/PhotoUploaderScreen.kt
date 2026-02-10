package com.truepineapps.photouploader.ui.screen.uploader

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
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.app_name
import com.truepineapps.photouploader.resources.photo_uploader
import com.truepineapps.photouploader.resources.select_photo_folder
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.components.ThemedIconButton
import com.truepineapps.photouploader.ui.navigation.NavigationDestination
import com.truepineapps.photouploader.ui.screen.LoadingScreen
import com.truepineapps.photouploader.ui.screen.uploader.uistate.UiState
import com.truepineapps.photouploader.ui.util.isExpandedWidth
import org.jetbrains.compose.resources.stringResource

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
) {
    val uiState by viewModel.uiState.collectAsState()
    val appName = stringResource(Res.string.app_name)

    // Update TopAppBar title when path changes or on initial composition
    LaunchedEffect(uiState.path) {
        val title = uiState.path.ifBlank { appName }
        onUpdateTopAppBar(title, null) {}
    }

    LoadingScreen(loadingViewModel = viewModel, modifier = modifier) {
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
