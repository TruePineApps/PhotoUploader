package com.truepineapps.photouploader.preview

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.ui.screen.uploader.components.PreviewPhotoCard
import com.truepineapps.photouploader.ui.theme.AppTheme
import com.truepineapps.photouploader.util.UiTextString
import okio.Path.Companion.toPath
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCard() {
    val photo = Photo(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.None
    )
    AppTheme {
        PreviewPhotoCard(photo)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCardError() {
    val photo = Photo(
        kmpFile = KmpFile(uri = "".toUri()),
        path = "/home".toPath(),
        name = "Test photo",
        isEnabled = true,
        isCoverPhoto = true,
        mediaItemId = null,
        uploadStatus = UploadStatus.Error(UiTextString("Photo Error message"))
    )
    AppTheme {
        PreviewPhotoCard(photo)
    }
}