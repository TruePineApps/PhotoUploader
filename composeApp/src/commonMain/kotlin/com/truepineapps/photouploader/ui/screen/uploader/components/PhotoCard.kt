package com.truepineapps.photouploader.ui.screen.uploader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.resources.Res
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.padding_small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = Dimensions.padding_small)
            ) {
                Checkbox(
                    checked = photo.isEnabled,
                    onCheckedChange = onCheckedChange,
                    enabled = isEditable
                )
                
                ThemedIconButton(
                    imageVector = if (photo.isCoverPhoto) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescriptionResource = Res.string.cover_photo,
                    enabled = isEditable,
                    onClick = { onCoverPhotoChange(photo) }
                )
            }

            // Remember the request based on the specific file.
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

            Text(
                text = photo.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            UploadStatusIndicator(
                uploadStatus = photo.uploadStatus,
                isAlbum = false,
                isEnabled = photo.isEnabled,
                modifier = Modifier.padding(start = Dimensions.padding_small)
            )
        }
    }
}
