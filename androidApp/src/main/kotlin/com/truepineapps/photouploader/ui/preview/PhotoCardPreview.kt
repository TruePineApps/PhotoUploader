package com.truepineapps.photouploader.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.feature.uploader.ui.components.PreviewPhotoCard
import com.truepineapps.photouploader.app.theme.AppTheme

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCardDefault() {
    AppTheme {
        PreviewPhotoCard(
            KmpFile(uri = "".toUri()),
            false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCardError() {
    AppTheme {
        PreviewPhotoCard(
            KmpFile(uri = "".toUri()),
            true
        )
    }
}