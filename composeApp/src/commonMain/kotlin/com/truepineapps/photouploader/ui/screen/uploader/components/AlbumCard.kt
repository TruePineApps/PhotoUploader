package com.truepineapps.photouploader.ui.screen.uploader.components

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
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.album_cover
import com.truepineapps.photouploader.resources.album_name
import com.truepineapps.photouploader.resources.loading_img
import com.truepineapps.photouploader.resources.photos_count
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.util.Opacity
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AlbumCard(
    album: Album,
    onAlbumClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditable = album.uploadStatus is UploadStatus.None || album.uploadStatus.isFinal

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.padding_very_small)
            .clickable(enabled = true) { onAlbumClick() }
            .alpha(
                if (album.isEnabled || album.uploadStatus == UploadStatus.Success)
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
                        checked = album.isEnabled,
                        onCheckedChange = onCheckedChange,
                        enabled = isEditable,
                    )

                    // Thumbnail is an AsyncImage
                    val context = LocalPlatformContext.current
                    val imageRequest = remember(album.coverPhoto) {
                        ImageRequest.Builder(context)
                            .data(album.coverPhoto.kmpFile)
                            .crossfade(true)
                            .memoryCacheKey(album.coverPhoto.path.toString())
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = stringResource(
                            Res.string.album_cover,
                            album.coverPhoto.getDisplayName()
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
                        value = album.name,
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
                    Text(
                        text = pluralStringResource(
                            Res.plurals.photos_count,
                            album.photos.size,
                            album.photos.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Dimensions.padding_very_small)
                    )
                }

                // Status Icon and status description
                UploadStatusIndicator(
                    uploadStatus = album.uploadStatus,
                    isAlbum = true,
                    isEnabled = album.isEnabled,
                    modifier = Modifier.padding(start = Dimensions.padding_small)
                )
            }

            // Bottom: Error Message, if any
            UploadErrorText(uploadStatus = album.uploadStatus)
        }
    }
}

@Composable
fun PreviewAlbumCard(album: Album) {
    AlbumCard(
        album = album,
        onAlbumClick = { },
        onCheckedChange = { },
        onNameChange = { },
    )
}