package com.truepineapps.photouploader.ui.screen.uploader.components

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
import androidx.compose.runtime.Composable
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
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.loading_img
import com.truepineapps.photouploader.resources.preview
import com.truepineapps.photouploader.ui.Dimensions
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PhotoCard(
    photo: Photo,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Checkbox(
                checked = photo.isEnabled,
                onCheckedChange = onCheckedChange
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photo.kmpFile)
                    .crossfade(true)
                    .build(),
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
        }
    }
}
