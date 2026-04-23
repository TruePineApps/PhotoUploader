package com.truepineapps.photouploader.feature.uploader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.album_name
import com.truepineapps.photouploader.resources.cover_photo
import com.truepineapps.photouploader.resources.loading_img
import com.truepineapps.photouploader.resources.preview
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.components.ThemedIconButton
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.PhotoUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadStatus
import com.truepineapps.photouploader.core.presentation.design.Opacity
import com.truepineapps.photouploader.core.util.UiTextString
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PhotoCard(
    photoUiState: PhotoUiState,
    onCheckedChange: (Boolean) -> Unit,
    onCoverPhotoChange: (PhotoUiState) -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditable = photoUiState.uploadStatus is UploadStatus.None

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.padding_very_small)
            .alpha(
                if (photoUiState.isEnabled || photoUiState.uploadStatus == UploadStatus.Success)
                    Opacity.FULL.value
                else
                    Opacity.DISABLED.value
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.padding_small)
        ) {
            // Top Row: Checkbox -> Image -> Name -> Favorite -> Status
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = photoUiState.isEnabled,
                        onCheckedChange = onCheckedChange,
                        enabled = isEditable,
                    )

                    val context = LocalPlatformContext.current
                    val imageRequest = remember(photoUiState.kmpFile) {
                        ImageRequest.Builder(context)
                            .data(photoUiState.kmpFile)
                            .crossfade(true)
                            .memoryCacheKey(photoUiState.path.toString())
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = stringResource(Res.string.preview),
                        error = rememberVectorPainter(Icons.Filled.BrokenImage),
                        placeholder = painterResource(Res.drawable.loading_img),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = Dimensions.padding_small)
                            .size(Dimensions.big_icon_size)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .weight(1f)
                        // No additional padding above text field
                        .offset(y = Dimensions.offset_text_field_vertical)
                ) {
                    TextField(
                        value = photoUiState.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(Res.string.album_name)) },
                        singleLine = true,
                        enabled = isEditable,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Favorite / Cover Action moved to the right side
                // Applied similar offset to align with the top text
                ThemedIconButton(
                    imageVector = if (photoUiState.isCoverPhoto) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescriptionResource = Res.string.cover_photo,
                    enabled = isEditable,
                    onClick = { onCoverPhotoChange(photoUiState) },
                    modifier = Modifier.offset(y = Dimensions.top_offset_themed_icon_button)
                )

                // Status Icon and status description
                UploadStatusIndicator(
                    uploadStatus = photoUiState.uploadStatus,
                    isAlbum = false,
                    isEnabled = photoUiState.isEnabled,
                    modifier = Modifier.padding(start = Dimensions.padding_small)
                )
            }

            // Bottom: Error Message, if any
            UploadErrorText(
                uploadStatus = photoUiState.uploadStatus
            )
        }
    }
}

@Composable
fun PreviewPhotoCard(dummyKmpFile: KmpFile, withError: Boolean) {
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
    PhotoCard(
        photoUiState = photoUiState,
        onCoverPhotoChange = { },
        onCheckedChange = { },
        onNameChange = { },
    )
}