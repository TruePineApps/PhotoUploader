package com.truepineapps.photouploader.ui.screen.uploader

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.screen.uploader.components.PhotoCard
import okio.Path

object PhotoListDestination {
    const val route = "photo_list"
    const val routeWithArgs = "$route/{path}"
}

@Composable
fun PhotoListContent(
    album: Album,
    onUpdateTopAppBar: (title: String, closeAction: (() -> Unit)?, actions: @Composable (RowScope.() -> Unit)) -> Unit,
    onBackClick: () -> Unit,
    onPhotoToggle: (Path) -> Unit,
    modifier: Modifier = Modifier
) {
    // Update the main TopAppBar with the album name and a back action
    LaunchedEffect(album.name) {
        onUpdateTopAppBar(
            album.name, 
            onBackClick // This will be used as the close/back action (displayed as back arrow or close icon depending on implementation)
        ) {}
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(album.photos, key = { it.path.toString() }) { photo ->
            PhotoCard(
                photo = photo,
                onCheckedChange = { onPhotoToggle(photo.path) },
                modifier = Modifier.padding(horizontal = Dimensions.padding_small)
            )
        }
    }
}
