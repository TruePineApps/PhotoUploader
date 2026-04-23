package com.truepineapps.photouploader.feature.uploader.ui.components

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadUiState
import kotlin.math.max
import kotlin.math.min

@Composable
fun ScrollLoadingInView(
    currentLastUploadingIndex: Int,
    totalItems: Int,
    isUploading: Boolean,
    key: List<UploadUiState>,
    lazyListState: LazyListState,
) {
    // State to track the last index that was tried to programmatically scroll to.
    // Used to detect if the user has scrolled away from a desired position.
    var lastKnownDesiredScrollIndex by remember { mutableStateOf(0) }

    LaunchedEffect(key, isUploading) {

        if (!isUploading || currentLastUploadingIndex == -1) {
            lastKnownDesiredScrollIndex =
                    lazyListState.firstVisibleItemIndex // Reset to current position
            return@LaunchedEffect
        }

        val itemsOnScreen =
                lazyListState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1) // Ensure at least 1 for calculation

        // If the entire list fits on the screen, no auto-scrolling is needed.programmatically
        if (totalItems <= itemsOnScreen) {
            lastKnownDesiredScrollIndex = 0 // Always at top
            return@LaunchedEffect
        }

        // Only count the last item if it is significantly visible to determine the row before last.
        val viewportHeight = lazyListState.layoutInfo.viewportEndOffset
        val lastVisibleItem =
                lazyListState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
        val lastVisibleItemCount =
                if (isSignificantlyVisible(
                        lastVisibleItem,
                        viewportHeight
                    )
                ) 0 else -1
        val itemsOnScreenCount =
                max(1, lazyListState.layoutInfo.visibleItemsInfo.size + lastVisibleItemCount)

        // Calculate the desired 'firstVisibleItemIndex' to achieve the 'one before last' layout for the uploading photo.
        val desiredFirstVisibleItemIndex =
                if (currentLastUploadingIndex < itemsOnScreenCount - 1) {
                    // Case: Uploading photo is near the beginning. Scroll to top.
                    0
                } else if (currentLastUploadingIndex >= totalItems - itemsOnScreenCount) {
                    // Case: Uploading photo is near the end. Scroll to show the last `itemsOnScreen` items.
                    totalItems - itemsOnScreenCount
                } else {
                    // Case: Uploading photo is in the middle. Place it as the one-to-last visible item.
                    max(0, currentLastUploadingIndex - (itemsOnScreenCount - 2))
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