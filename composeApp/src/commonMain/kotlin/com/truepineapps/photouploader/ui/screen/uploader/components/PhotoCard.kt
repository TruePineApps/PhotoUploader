package com.truepineapps.photouploader.ui.screen.uploader.components

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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.album_name
import com.truepineapps.photouploader.resources.cover_photo
import com.truepineapps.photouploader.resources.loading_img
import com.truepineapps.photouploader.resources.preview
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.components.ThemedIconButton
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PhotoCard(
    photo: Photo,
    onCheckedChange: (Boolean) -> Unit,
    onCoverPhotoChange: (Photo) -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditable = photo.uploadStatus is UploadStatus.None

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.padding_very_small),
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
                .padding(Dimensions.padding_small
//                    start = Dimensions.padding_small,
//                    end = Dimensions.padding_small,
//                    top = Dimensions.padding_small,
                    // No padding at the bottom since the row is hoisted because of the negative
                    // offset of the TextField column
                )
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
                        checked = photo.isEnabled,
                        onCheckedChange = onCheckedChange,
                        enabled = isEditable,
                    )

                    val context = LocalPlatformContext.current
                    val imageRequest = remember(photo.kmpFile) {
                        ImageRequest.Builder(context)
                            .data(photo.kmpFile)
                            .crossfade(true)
                            .memoryCacheKey(photo.path.toString())
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
                        value = photo.name,
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
                    imageVector = if (photo.isCoverPhoto) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescriptionResource = Res.string.cover_photo,
                    enabled = isEditable,
                    onClick = { onCoverPhotoChange(photo) },
                    modifier = Modifier.offset(y = Dimensions.top_offset_themed_icon_button)
                )

                // Status Icon and status description
                UploadStatusIndicator(
                    uploadStatus = photo.uploadStatus,
                    isAlbum = false,
                    isEnabled = photo.isEnabled,
                    modifier = Modifier.padding(start = Dimensions.padding_small)
                )
            }

            // Bottom: Error Message, if any
            UploadErrorText(uploadStatus = photo.uploadStatus)
        }
    }
}

@Composable
fun PreviewPhotoCard(photo: Photo) {
    PhotoCard(
        photo = photo,
        onCoverPhotoChange = { },
        onCheckedChange = { },
        onNameChange = { },
    )
}