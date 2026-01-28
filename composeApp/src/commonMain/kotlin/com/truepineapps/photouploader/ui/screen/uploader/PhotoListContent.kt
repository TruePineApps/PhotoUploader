package com.truepineapps.photouploader.ui.screen.uploader

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.ui.screen.uploader.uistate.AlbumUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.PhotoUiState
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.screen.uploader.components.PhotoCard
import okio.Path

object PhotoListDestination {
    const val route = "photo_list"
    const val routeWithArgs = "$route/{path}"
}

@Composable
fun PhotoListScreen(
    albumId: String?,
    onUpdateTopAppBar: (title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PhotoUploaderViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val album = uiState.albumUiStates.find { it.id == albumId }
    if (album != null) {
        PhotoListContent(
            albumUiState = album,
            onUpdateTopAppBar = onUpdateTopAppBar,
            onPhotoToggle = { photoPath -> viewModel.togglePhoto(album.id, photoPath) },
            onCoverPhotoChange = { photo -> viewModel.updateCoverPhoto(album.id, photo) },
            onPhotoNameChange = { photo, name -> viewModel.renamePhoto(album.id, photo.path, name) },
            modifier = modifier
        )
    } else {
        // Album not found (e.g. because directory changed and we rescanned).
        // Navigate back to the main list.
        LaunchedEffect(Unit) { onBackClick() }
    }
}

@Composable
fun PhotoListContent(
    albumUiState: AlbumUiState,
    onUpdateTopAppBar: (title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit,
    onPhotoToggle: (Path) -> Unit,
    onCoverPhotoChange: (PhotoUiState) -> Unit,
    onPhotoNameChange: (PhotoUiState, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Update the main TopAppBar with the album name and a back action
    LaunchedEffect(albumUiState.name) {
        onUpdateTopAppBar(albumUiState.name, null) {}
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
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
