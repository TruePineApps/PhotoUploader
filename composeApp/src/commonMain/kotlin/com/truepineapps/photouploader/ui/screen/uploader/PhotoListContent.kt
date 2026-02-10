package com.truepineapps.photouploader.ui.screen.uploader

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.screen.uploader.components.PhotoCard
import com.truepineapps.photouploader.ui.screen.uploader.components.ScrollLoadingInView
import com.truepineapps.photouploader.ui.screen.uploader.uistate.AlbumUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.PhotoUiState
import okio.Path

object PhotoListDestination {
    const val route = "photo_list"
    const val routeWithArgs = "$route/{path}"
}

@Composable
fun PhotoListScreen(
    albumId: String?,
    onUpdateTopAppBar: ((title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit)?,
    onBackClick: () -> Unit,
    viewModel: PhotoUploaderViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val albumUiState = uiState.albumUiStates.find { it.id == albumId }
    if (albumUiState != null) {
        val isUploading = uiState.isUploading
        if (albumUiState.photoUiStates.isNotEmpty()) {
            PhotoListContent(
                albumUiState = albumUiState,
                isUploading = isUploading,
                onUpdateTopAppBar = onUpdateTopAppBar,
                onPhotoToggle = { photoPath -> viewModel.togglePhoto(albumUiState.id, photoPath) },
                onCoverPhotoChange = { photo ->
                    viewModel.updateCoverPhoto(
                        albumUiState.id,
                        photo
                    )
                },
                onPhotoNameChange = { photo, name ->
                    viewModel.renamePhoto(
                        albumUiState.id,
                        photo.path,
                        name
                    )
                },
                modifier = modifier
            )
        }
    } else {
        // Album not found (e.g. because directory changed and we rescanned).
        // Navigate back to the main list.
        LaunchedEffect(Unit) { onBackClick() }
    }
}

@Composable
private fun PhotoListContent(
    albumUiState: AlbumUiState,
    isUploading: Boolean,
    onUpdateTopAppBar: ((title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit)?,
    onPhotoToggle: (Path) -> Unit,
    onCoverPhotoChange: (PhotoUiState) -> Unit,
    onPhotoNameChange: (PhotoUiState, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // When single screen, update the main TopAppBar with the album name and a back action
    if (onUpdateTopAppBar != null) {
        LaunchedEffect(albumUiState.name) {
            onUpdateTopAppBar(albumUiState.name, null) {}
        }
    }

    val lazyListState = rememberLazyListState()

    ScrollLoadingInView(
        currentLastUploadingIndex = albumUiState.photoUiStates.indexOfLast { it.uploadStatus.isUploading },
        totalItems = albumUiState.photoUiStates.size,
        isUploading = isUploading,
        key = albumUiState.photoUiStates,
        lazyListState = lazyListState
    )

    LazyColumn(state = lazyListState, modifier = modifier) {
        items(albumUiState.photoUiStates, key = { it.path.toString() }) { photo ->
            PhotoCard(
                photoUiState = photo,
                onCheckedChange = { onPhotoToggle(photo.path) },
                onCoverPhotoChange = onCoverPhotoChange,
                onNameChange = { newName -> onPhotoNameChange(photo, newName) },
                modifier = Modifier.padding(horizontal = Dimensions.padding_small)
            )
        }
    }
}

