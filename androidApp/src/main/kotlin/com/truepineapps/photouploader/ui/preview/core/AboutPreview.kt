package com.truepineapps.photouploader.ui.preview.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.feature.moremenu.ui.AboutScreenAndroidPreview

@Preview(showBackground = true)
@Composable
fun PreviewAboutScreen() {
    AppTheme { AboutScreenAndroidPreview() }
}