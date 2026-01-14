package com.truepineapps.photouploader.ui.screen.uploader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.collapse_album
import com.truepineapps.photouploader.resources.expand_album
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.components.ThemedIconButton
import com.truepineapps.photouploader.ui.screen.uploader.components.AlbumCard
import com.truepineapps.photouploader.ui.util.Opacity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumListContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAlbumToggle: (String) -> Unit,
    onAlbumGroupToggle: (List<Album>, Boolean) -> Unit,
    onAlbumRename: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedAlbums = albums.groupBy { it.group }
    val expansionState = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        groupedAlbums.forEach { (group, albumsInGroup) ->
            stickyHeader(key = group) {
                var isChecked by remember(group) { mutableStateOf(true) }
                val isExpanded = expansionState[group] ?: true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(Dimensions.padding_small)
                        .alpha(if (albumsInGroup.any { it.isEnabled }) Opacity.FULL.value else Opacity.DISABLED.value),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            isChecked = checked
                            onAlbumGroupToggle(albumsInGroup, checked)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            uncheckedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            checkmarkColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    ThemedIconButton(
                        onClick = { expansionState[group] = !isExpanded },
                        imageVector = if (isExpanded) Icons.Filled.UnfoldLess else Icons.Filled.UnfoldMore,
                        contentDescriptionResource = if (isExpanded) Res.string.collapse_album else Res.string.expand_album,
                        enabled = true
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.padding_minimum))
            }

            if (expansionState[group] ?: true) {
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
}
