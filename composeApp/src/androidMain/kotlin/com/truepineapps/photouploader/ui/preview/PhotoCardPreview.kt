package com.truepineapps.photouploader.ui.preview

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.ui.screen.uploader.uistate.PhotoUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.UploadStatus
import com.truepineapps.photouploader.ui.screen.uploader.components.PreviewPhotoCard
import com.truepineapps.photouploader.ui.theme.AppTheme
import com.truepineapps.photouploader.util.UiTextString
import okio.Path.Companion.toPath
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCard() {
    val photoUiState = PhotoUiState(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.None
    )
    AppTheme {
        PreviewPhotoCard(photoUiState)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCardError() {
    val photoUiState = PhotoUiState(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.Error(UiTextString("Photo Error message"))
    )
    AppTheme {
        PreviewPhotoCard(photoUiState)
    }
}