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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.collapse_album
import com.truepineapps.photouploader.resources.expand_album
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.component.ThemedIconButton
import com.truepineapps.photouploader.feature.uploader.ui.components.AlbumCard
import com.truepineapps.photouploader.feature.uploader.ui.components.ScrollLoadingInView
import com.truepineapps.photouploader.feature.uploader.ui.components.UploadStatusIndicator
import com.truepineapps.photouploader.core.presentation.design.Opacity
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.AlbumUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.GroupUiState
import kotlin.collections.indexOfLast

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumListContent(
    groupUiStates: List<GroupUiState>,
    selectedAlbumId: String,
    isUploading: Boolean,
    onAlbumClick: (AlbumUiState) -> Unit,
    onAlbumToggle: (String) -> Unit,
    onAlbumRename: (String, String) -> Unit,
    onAlbumGroupToggle: (GroupUiState, Boolean) -> Unit,
    onAlbumGroupExpanded: (GroupUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()

    // Count items for scroll position calculation
    var currentLastUploadingIndex = -1
    var totalItems = 0
    groupUiStates.forEach { groupUiState ->
        // Calculate currentLastUploadingIndex first; totalItems is count up to this group
        if (groupUiState.uploadStatus.isUploading) {
            if (groupUiState.isExpanded) {
                val lastUploadingAlbumIndex =
                        groupUiState.albumsInGroup.indexOfLast { it.uploadStatus.isUploading }
                currentLastUploadingIndex = totalItems + lastUploadingAlbumIndex + 1
            } else {
                currentLastUploadingIndex = totalItems
            }
        }
        totalItems += if (groupUiState.isExpanded) groupUiState.albumsInGroup.size + 1 else 1
    }

    ScrollLoadingInView(
        currentLastUploadingIndex = currentLastUploadingIndex,
        totalItems = totalItems,
        isUploading = isUploading,
        key = groupUiStates,
        lazyListState = lazyListState
    )

    LazyColumn(state = lazyListState, modifier = modifier) {
        groupUiStates.forEach { groupUiState ->
            val group = groupUiState.group
            val albumsInGroup = groupUiState.albumsInGroup
            val isExpanded = groupUiState.isExpanded
            stickyHeader(key = group) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(Dimensions.padding_small)
                        .alpha(if (albumsInGroup.any { it.isEnabled }) Opacity.FULL.value else Opacity.DISABLED.value),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = groupUiState.isEnabled,
                        onCheckedChange = { newChecked ->
                            onAlbumGroupToggle(groupUiState, newChecked)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            uncheckedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            checkmarkColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    // Status Icon and status description
                    UploadStatusIndicator(
                        uploadStatus = groupUiState.uploadStatus,
                        isAlbum = true,
                        isEnabled = groupUiState.isEnabled,
                        modifier = Modifier.padding(start = Dimensions.padding_small)
                    )
                    // Collapse/Expand button
                    ThemedIconButton(
                        onClick = { onAlbumGroupExpanded(groupUiState) },
                        imageVector = if (isExpanded) Icons.Filled.UnfoldLess else Icons.Filled.UnfoldMore,
                        contentDescriptionResource = if (isExpanded) Res.string.collapse_album else Res.string.expand_album,
                        enabled = true
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.padding_minimum))
            }

            if (isExpanded) {
                items(albumsInGroup, key = { it.id }) { album ->
                    AlbumCard(
                        albumUiState = album,
                        isSelected = (selectedAlbumId == album.id),
                        onAlbumClick = { onAlbumClick(album) },
                        onCheckedChange = { onAlbumToggle(album.id) },
                        onNameChange = { newName -> onAlbumRename(album.id, newName) },
                        modifier = Modifier.padding(horizontal = Dimensions.padding_small)
                    )
                }
            }
        }
    }
}
