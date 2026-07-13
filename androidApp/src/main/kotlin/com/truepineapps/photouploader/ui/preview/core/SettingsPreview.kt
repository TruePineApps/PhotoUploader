package com.truepineapps.photouploader.ui.preview.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.feature.settings.ui.SettingsScreenPreview

@Preview(showBackground = true)
@Composable
fun PreviewLicenseScreen() {
    AppTheme { SettingsScreenPreview() }
}