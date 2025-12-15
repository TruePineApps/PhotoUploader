package com.truepineapps.photouploader.ui.screen.uploader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.ui.Dimensions
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PhotoCard(
    photo: Photo,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: ImageBitmap? = null // Placeholder for now
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

             // Placeholder for Thumbnail
            Column(
                 modifier = Modifier
                     .size(Dimensions.big_icon_size)
                     .padding(end = Dimensions.padding_small),
                 horizontalAlignment = Alignment.CenterHorizontally,
                 verticalArrangement = Arrangement.Center
            ) {
                 Text("Img", style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = photo.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
