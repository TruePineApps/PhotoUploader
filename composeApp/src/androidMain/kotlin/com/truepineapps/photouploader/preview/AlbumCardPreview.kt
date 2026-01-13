package com.truepineapps.photouploader.preview

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.ui.screen.uploader.components.PreviewAlbumCard
import com.truepineapps.photouploader.ui.theme.AppTheme
import com.truepineapps.photouploader.util.UiTextString
import okio.Path.Companion.toPath
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PreviewAlbumCard() {
    val photo = Photo(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.None
    )
    val album = Album(
        id = "1",
        name = "2026 - Test album",
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        group = "2026",
        photos = emptyList(),
        coverPhoto = photo,
        isEnabled = true,
        albumId = "",
        uploadStatus = UploadStatus.None
    )
    AppTheme {
        PreviewAlbumCard(album)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAlbumCardError() {
    val photo = Photo(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.Error(UiTextString("Photo Error message"))
    )
    val album = Album(
        id = "1",
        name = "2026 - This is a Test album with a very long name",
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        group = "2026",
        photos = emptyList(),
        coverPhoto = photo,
        isEnabled = true,
        albumId = "",
        uploadStatus = UploadStatus.Error(UiTextString("Album Error message"))
    )
    AppTheme {
        PreviewAlbumCard(album)
    }
}