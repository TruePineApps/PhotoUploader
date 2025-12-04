package com.truepineapps.photouploader.ui.screen.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.license_text
import com.truepineapps.photouploader.resources.licenses
import com.truepineapps.photouploader.resources.loading
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.navigation.NavigationDestination
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

object LicenseDestination : NavigationDestination {
    override val route = "license"
    override val titleRes = Res.string.licenses
}

@Composable
fun LicenseScreen(
    modifier: Modifier = Modifier,
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit = { _, _, _ -> },
) {
    val loadingMessage = stringResource(
        Res.string.loading,
        stringResource(Res.string.license_text)
    )
    var licenseText by remember { mutableStateOf(loadingMessage) }

    onUpdateTopAppBar(stringResource(LicenseDestination.titleRes), null) {}

    LaunchedEffect(Unit) {
        licenseText = loadFontLicense()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.padding_medium)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Font Licenses",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = Dimensions.padding_small)
        )
        Text(
            text = "Noto Sans",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dimensions.padding_very_small))
        // Format license text in monospace font
        Text(
            text = licenseText,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
suspend fun loadFontLicense(): String {

    val bytes = Res.readBytes("files/OFL.txt")
    return bytes.decodeToString()
}
