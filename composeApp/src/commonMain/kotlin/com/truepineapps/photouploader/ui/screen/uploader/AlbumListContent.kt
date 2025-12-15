package com.truepineapps.photouploader.ui.screen.uploader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.screen.uploader.components.AlbumCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumListContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAlbumToggle: (String) -> Unit,
    onAlbumRename: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedAlbums = albums.groupBy { it.group }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        groupedAlbums.forEach { (group, albumsInGroup) ->
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(Dimensions.padding_small)
                ) {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            items(albumsInGroup, key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    onAlbumClick = { onAlbumClick(album) },
                    onCheckedChange = { onAlbumToggle(album.id) },
                    onNameChange = { newName -> onAlbumRename(album.id, newName) },
                    modifier = Modifier.padding(horizontal = Dimensions.padding_small)
                )
            }
        }
    }
}
