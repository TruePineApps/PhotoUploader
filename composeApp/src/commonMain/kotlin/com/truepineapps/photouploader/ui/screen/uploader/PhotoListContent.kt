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
    modifier: Modifier = Modifier
) {
    // When single screen, update the main TopAppBar with the album name and a back action
    if (onUpdateTopAppBar != null) {
        LaunchedEffect(albumUiState.name) {
            onUpdateTopAppBar(albumUiState.name, null) {}
        }
    }

    val lazyListState = rememberLazyListState()

    // State to track if the user has manually scrolled, disabling auto-scroll
    var userHasManuallyScrolled by remember { mutableStateOf(false) }

    // State to track the last index we tried to scroll to programmatically.
    // Used to detect if the user has scrolled away from a desired position.
    var lastKnownDesiredScrollIndex by remember { mutableStateOf(0) }

    LaunchedEffect(albumUiState.photoUiStates, isUploading) {
        val currentLastUploadingPhotoIndex = albumUiState.photoUiStates.indexOfLast { it.uploadStatus.isUploading }

        if (isUploading && currentLastUploadingPhotoIndex != -1) {
            // No automatic scrolling if the user scrolled manually.
            if (userHasManuallyScrolled) {
                return@LaunchedEffect
            }

            val totalPhotos = albumUiState.photoUiStates.size
            val itemsOnScreen = lazyListState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1) // Ensure at least 1 for calculation

            // If the entire list fits on the screen, no auto-scrolling is needed.
            if (totalPhotos <= itemsOnScreen) {
                userHasManuallyScrolled = false // Reset in case it was set during a prior manual scroll
                lastKnownDesiredScrollIndex = 0 // Always at top
                return@LaunchedEffect
            }

            // Only count items that are significantly visible to determine the row before last.
            val viewportHeight = lazyListState.layoutInfo.viewportEndOffset
            val significantlyVisibleItems = lazyListState.layoutInfo.visibleItemsInfo.filter { itemInfo ->
                val itemHeight = itemInfo.size
                val itemOffset = itemInfo.offset

                val visibleStart = max(itemOffset, 0)
                val visibleEnd = min(itemOffset + itemHeight, viewportHeight)
                val visibleHeight = max(0, visibleEnd - visibleStart)

                // Threshold: at least 50% visible
                visibleHeight >= itemHeight * 0.5f
            }
            val itemsOnScreenCount = significantlyVisibleItems.size.coerceAtLeast(1) // Use count of significantly visible items

            val firstVisibleItem = lazyListState.firstVisibleItemIndex
            val firstVisibleItemOffset = lazyListState.firstVisibleItemScrollOffset

            // Calculate the desired 'firstVisibleItemIndex' to achieve the 'one before last' layout for the uploading photo.
            val desiredFirstVisibleItemIndex = if (currentLastUploadingPhotoIndex < itemsOnScreenCount - 1) {
                // Case: Uploading photo is near the beginning. Scroll to top.
                0
            } else if (currentLastUploadingPhotoIndex >= totalPhotos - itemsOnScreenCount) {
                // Case: Uploading photo is near the end. Scroll to show the last `itemsOnScreen` items.
                totalPhotos - itemsOnScreenCount
            } else {
                // Case: Uploading photo is in the middle. Place it as the second-to-last visible item.
                max(0, currentLastUploadingPhotoIndex - (itemsOnScreenCount - 2))
            }

            // Determine if the user has manually scrolled away from a previously desired position.
            // This happens if we had a target, but the current scroll position no longer matches.
            userHasManuallyScrolled = (firstVisibleItem != lastKnownDesiredScrollIndex || firstVisibleItemOffset != 0)
            if (!userHasManuallyScrolled ) {
                // Only perform programmatic scroll if the view is not already in the desired position.
                if (!(firstVisibleItem == desiredFirstVisibleItemIndex && firstVisibleItemOffset == 0)) {
                    lazyListState.animateScrollToItem(desiredFirstVisibleItemIndex)
                    lastKnownDesiredScrollIndex = desiredFirstVisibleItemIndex
                } else {
                    lastKnownDesiredScrollIndex = firstVisibleItem
                }
            }
        } else {
            // If not uploading or no photos uploading, ensure auto-scroll is re-enabled for next upload
            // and reset the last known desired scroll index to the current position.
            userHasManuallyScrolled = false
            lastKnownDesiredScrollIndex = lazyListState.firstVisibleItemIndex
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