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

package com.truepineapps.photouploader.feature.uploader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.design.LocalExtendedColors
import com.truepineapps.photouploader.core.presentation.design.Opacity
import com.truepineapps.photouploader.core.util.UiTextString
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.AlbumUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.PhotoUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadStatus
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.album_cover
import com.truepineapps.photouploader.resources.album_name
import com.truepineapps.photouploader.resources.loading_img
import com.truepineapps.photouploader.resources.photos_count
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AlbumCard(
    albumUiState: AlbumUiState,
    isSelected: Boolean,
    onAlbumClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditable = albumUiState.uploadStatus is UploadStatus.None || albumUiState.uploadStatus.isFinal

    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = LocalExtendedColors.current.selectedItemHighlight,
            contentColor = MaterialTheme.colorScheme.scrim
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.padding_very_small)
            .clickable(enabled = true) { onAlbumClick() }
            .alpha(
                if (albumUiState.isEnabled || albumUiState.uploadStatus == UploadStatus.Success)
                    Opacity.FULL.value
                else
                    Opacity.DISABLED.value
            ),
        colors = cardColors
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimensions.padding_small,
                    end = Dimensions.padding_small,
                    top = Dimensions.padding_small,
                    // No padding at the bottom since the row is hoisted because of the negative
                    // offset of the TextField column
                )
        ) {
            // Top Row: Checkbox, Image, Title, Status Icon
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = albumUiState.isEnabled,
                        onCheckedChange = onCheckedChange,
                        enabled = isEditable,
                    )

                    // Thumbnail is an AsyncImage
                    val context = LocalPlatformContext.current
                    val imageRequest = remember(albumUiState.coverPhotoUiState) {
                        ImageRequest.Builder(context)
                            .data(albumUiState.coverPhotoUiState.kmpFile)
                            .crossfade(true)
                            .memoryCacheKey(albumUiState.coverPhotoUiState.path.toString())
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = stringResource(
                            Res.string.album_cover,
                            albumUiState.coverPhotoUiState.getDisplayName()
                        ),
                        error = rememberVectorPainter(Icons.Filled.BrokenImage),
                        placeholder = painterResource(Res.drawable.loading_img),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = Dimensions.padding_small)
                            .size(Dimensions.big_icon_size)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .weight(1f)
                        // No additional padding above text field
                        .offset(y = Dimensions.offset_text_field_vertical)
                ) {
                    TextField(
                        value = albumUiState.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(Res.string.album_name)) },
                        singleLine = true,
                        enabled = isEditable,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = cardColors.containerColor,
                            disabledContainerColor = cardColors.containerColor,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = cardColors.contentColor,
                            disabledTextColor = cardColors.contentColor,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = pluralStringResource(
                            Res.plurals.photos_count,
                            albumUiState.photoUiStates.size,
                            albumUiState.photoUiStates.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Dimensions.padding_very_small)
                    )
                }

                // Status Icon and status description
                UploadStatusIndicator(
                    uploadStatus = albumUiState.uploadStatus,
                    isAlbum = true,
                    isEnabled = albumUiState.isEnabled,
                    modifier = Modifier.padding(start = Dimensions.padding_small)
                )
            }

            // Bottom: Error Message, if any
            UploadErrorText(
                uploadStatus = albumUiState.uploadStatus
            )
        }
    }
}

@Composable
fun PreviewAlbumCard(dummyKmpFile: KmpFile, withError: Boolean) {
    val photoUiState =
        PhotoUiState(
            kmpFile = dummyKmpFile,
            path = "/home".toPath(),
            name = "Test photo",
            isEnabled = true,
            isCoverPhoto = true,
            mediaItemId = null,
            uploadStatus = if (withError) UploadStatus.Error(
                UiTextString("Photo Error message")
            ) else UploadStatus.None
        )
    val albumUiState =
        AlbumUiState(
            id = "1",
            name = "2026 - This is a Test album with a very long name",
            kmpFile = dummyKmpFile,
            path = "/home".toPath(),
            group = "2026",
            photoUiStates = emptyList(),
            coverPhotoUiState = photoUiState,
            isEnabled = true,
            googleAlbumId = "",
            uploadStatus = if (withError) UploadStatus.Error(
                UiTextString("Album Error message")
            ) else UploadStatus.None
        )

    AlbumCard(
        albumUiState = albumUiState,
        isSelected = false,
        onAlbumClick = { },
        onCheckedChange = { },
        onNameChange = { },
    )
}