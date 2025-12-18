package com.truepineapps.photouploader.ui.screen.uploader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext as CoilContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mohamedrejeb.calf.core.LocalPlatformContext as KmpFileContext
import com.truepineapps.photouploader.io.getAbsolutePath
import com.truepineapps.photouploader.io.getDisplayName
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.album_cover
import com.truepineapps.photouploader.resources.loading_img
import com.truepineapps.photouploader.ui.Dimensions
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.padding_very_small)
            .clickable { onAlbumClick() },
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
            Checkbox(
                checked = album.isEnabled,
                onCheckedChange = onCheckedChange
            )

            val coilContext = CoilContext.current
            val kmpFileContext = KmpFileContext.current
            // Remember the request to prevent rebuilding it on every frame if other props change
            val imageRequest = remember(album.coverPhoto) {
                ImageRequest.Builder(coilContext)
                    .data(album.coverPhoto)
                    .crossfade(true)
                    // Prevent choppy scrolling by enforcing a specific memory key
                    .memoryCacheKey(album.coverPhoto.kmpFile.getAbsolutePath(kmpFileContext))
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = stringResource(
                    Res.string.album_cover,
                    album.coverPhoto.kmpFile.getDisplayName(kmpFileContext)
                ),
                error = rememberVectorPainter(Icons.Filled.BrokenImage),
                placeholder = painterResource(Res.drawable.loading_img),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = Dimensions.padding_small)
                    .size(Dimensions.big_icon_size)
            )

            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    value = album.name,
                    onValueChange = onNameChange,
                    label = { Text("Album Name") },
                    singleLine = true
                )
                Text(
                    text = "${album.photos.size} photos",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Dimensions.padding_very_small)
                )
            }
        }
    }
}
