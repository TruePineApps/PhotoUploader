package com.truepineapps.photouploader.ui.screen.uploader.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.ui.Dimensions
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AlbumCard(
    album: Album,
    onAlbumClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: ImageBitmap? = null // In a real app, load this asynchronously
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
            
            // Placeholder for Thumbnail (In real implementation, load image from first photo path)
             // For now, we just show a box or text if no image logic is plugged in yet
             // Using a simple box for layout
            Column(
                modifier = Modifier
                     .size(Dimensions.big_icon_size)
                     .padding(end = Dimensions.padding_small),
                 horizontalAlignment = Alignment.CenterHorizontally,
                 verticalArrangement = Arrangement.Center
            ) {
                 Text("Img", style = MaterialTheme.typography.bodySmall)
            }

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
