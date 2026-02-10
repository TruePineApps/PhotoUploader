package com.truepineapps.photouploader.ui.screen.uploader

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.screen.uploader.components.PhotoCard
import com.truepineapps.photouploader.ui.screen.uploader.uistate.AlbumUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.PhotoUiState
import okio.Path
import kotlin.math.max
import kotlin.math.min

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

    // State to track the last index we tried to scroll to programmatically.
    // Used to detect if the user has scrolled away from a desired position.
    var lastKnownDesiredScrollIndex by remember { mutableStateOf(0) }

    LaunchedEffect(albumUiState.photoUiStates, isUploading) {
        val currentLastUploadingPhotoIndex =
                albumUiState.photoUiStates.indexOfLast { it.uploadStatus.isUploading }

        if (!isUploading || currentLastUploadingPhotoIndex == -1) {
            lastKnownDesiredScrollIndex =
                    lazyListState.firstVisibleItemIndex // Reset to current position
            return@LaunchedEffect
        }

        val totalPhotos = albumUiState.photoUiStates.size
        val itemsOnScreen =
                lazyListState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1) // Ensure at least 1 for calculation

        // If the entire list fits on the screen, no auto-scrolling is needed.
        if (totalPhotos <= itemsOnScreen) {
            lastKnownDesiredScrollIndex = 0 // Always at top
            return@LaunchedEffect
        }

        // Only count the last item if it is significantly visible to determine the row before last.
        val viewportHeight = lazyListState.layoutInfo.viewportEndOffset
        val lastVisibleItem =
                lazyListState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
        val lastVisibleItemCount = if (isSignificantlyVisible(lastVisibleItem, viewportHeight)) 0 else -1
        val itemsOnScreenCount = max(1, lazyListState.layoutInfo.visibleItemsInfo.size + lastVisibleItemCount)

        // Calculate the desired 'firstVisibleItemIndex' to achieve the 'one before last' layout for the uploading photo.
        val desiredFirstVisibleItemIndex =
            if (currentLastUploadingPhotoIndex < itemsOnScreenCount - 1) {
                // Case: Uploading photo is near the beginning. Scroll to top.
                0
            } else if (currentLastUploadingPhotoIndex >= totalPhotos - itemsOnScreenCount) {
                // Case: Uploading photo is near the end. Scroll to show the last `itemsOnScreen` items.
                totalPhotos - itemsOnScreenCount
            } else {
                // Case: Uploading photo is in the middle. Place it as the one-to-last visible item.
                max(0, currentLastUploadingPhotoIndex - (itemsOnScreenCount - 2))
            }

        // Do not scroll if the user scrolled manually; continue when the desired position is
        // the same as the current position.
        val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
        val firstVisibleItemOffset = lazyListState.firstVisibleItemScrollOffset
        val autoScrollEnabled = if (lazyListState.isScrollInProgress) {
            // If the user is actively scrolling, pause auto-scroll.
            false
        } else if (firstVisibleItemIndex == lastKnownDesiredScrollIndex && firstVisibleItemOffset == 0) {
            // No change since the last auto-scroll, continue since no scrolling in progress
            true
        } else {
            // The user has manually scrolled away from a previous autoscroll position.
            // Continue if the new desired position matches the actual position.
            desiredFirstVisibleItemIndex == firstVisibleItemIndex
        }

        if (autoScrollEnabled) {
            // Only perform programmatic scroll if the view is not already in the desired position.
            if (!(firstVisibleItemIndex == desiredFirstVisibleItemIndex && firstVisibleItemOffset == 0)) {
                lazyListState.animateScrollToItem(desiredFirstVisibleItemIndex)
                lastKnownDesiredScrollIndex = desiredFirstVisibleItemIndex
            } else {
                lastKnownDesiredScrollIndex = firstVisibleItemIndex
            }
        }
    }

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

private fun isSignificantlyVisible(
    itemInfo: LazyListItemInfo,
    viewportHeight: Int,
    threshold: Float = 0.5f
): Boolean {
    val itemHeight = itemInfo.size
    val itemOffset = itemInfo.offset

    val visibleStart = max(itemOffset, 0)
    val visibleEnd = min(itemOffset + itemHeight, viewportHeight)
    val visibleHeight = max(0, visibleEnd - visibleStart)

    // Threshold: at least 50% visible
    return visibleHeight >= itemHeight * threshold
}