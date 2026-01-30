package com.truepineapps.photouploader.ui.preview

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.ui.screen.uploader.uistate.AlbumUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.PhotoUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.UploadStatus
import com.truepineapps.photouploader.ui.screen.uploader.components.PreviewAlbumCard
import com.truepineapps.photouploader.ui.theme.AppTheme
import com.truepineapps.photouploader.util.UiTextString
import okio.Path.Companion.toPath
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PreviewAlbumCard() {
    val photoUiState = PhotoUiState(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.None
    )
    val albumUiState = AlbumUiState(
        id = "1",
        name = "2026 - Test album",
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        group = "2026",
        photoUiStates = emptyList(),
        coverPhotoUiState = photoUiState,
        isEnabled = true,
        googleAlbumId = "",
        uploadStatus = UploadStatus.None
    )
    AppTheme {
        PreviewAlbumCard(albumUiState)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAlbumCardError() {
    val photoUiState = PhotoUiState(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.Error(UiTextString("Photo Error message"))
    )
    val albumUiState = AlbumUiState(
        id = "1",
        name = "2026 - This is a Test album with a very long name",
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        group = "2026",
        photoUiStates = emptyList(),
        coverPhotoUiState = photoUiState,
        isEnabled = true,
        googleAlbumId = "",
        uploadStatus = UploadStatus.Error(UiTextString("Album Error message"))
    )
    AppTheme {
        PreviewAlbumCard(albumUiState)
    }
}