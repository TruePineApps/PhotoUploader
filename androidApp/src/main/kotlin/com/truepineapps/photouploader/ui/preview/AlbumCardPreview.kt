package com.truepineapps.photouploader.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.feature.uploader.ui.components.PreviewAlbumCard
import com.truepineapps.photouploader.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
fun PreviewAlbumCardDefault() {

    AppTheme {
        PreviewAlbumCard(
            KmpFile(uri = "".toUri()),
            false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAlbumCardError() {
    AppTheme {
        PreviewAlbumCard(
            KmpFile(uri = "".toUri()),
            true
        )
    }
}