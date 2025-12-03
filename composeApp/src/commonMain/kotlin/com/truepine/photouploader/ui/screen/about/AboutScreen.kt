/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepine.photouploader.ui.screen.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.truepine.photouploader.ui.theme.AppTheme
import com.truepine.photouploader.AppInfo
import com.truepine.photouploader.PlatformAppInfo
import com.truepine.photouploader.PlatformInfo
import com.truepine.photouploader.ui.Dimensions
import com.truepine.photouploader.resources.Res
import com.truepine.photouploader.resources.about
import com.truepine.photouploader.resources.about_photo_uploader
import com.truepine.photouploader.resources.app_id
import com.truepine.photouploader.resources.platform
import com.truepine.photouploader.resources.version
import com.truepine.photouploader.ui.navigation.NavigationDestination
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

object AboutDestination : NavigationDestination {
    override val route = "about"
    override val titleRes = Res.string.about
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit = { _, _, _ -> },
    appInfo: AppInfo = koinInject(),
    platformInfo: PlatformInfo = koinInject(),
) {
    onUpdateTopAppBar(stringResource(AboutDestination.titleRes), null) {}

    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.about_photo_uploader),
            style = MaterialTheme.typography.titleLarge,
        )
        PlatformDetailRow(Res.string.version, "${appInfo.versionName} (${appInfo.versionCode})")
        PlatformDetailRow(Res.string.app_id, appInfo.appId)
        PlatformDetailRow(Res.string.platform, platformInfo.name)
        Spacer(modifier = Modifier.height(Dimensions.padding_medium))
        Text(
            text = "The event app is a demo project to show how to build an app that runs on different platforms."
        )
    }

}

@Composable
private fun PlatformDetailRow(
    labelResId: StringResource, detail: String?, modifier: Modifier = Modifier,
) {
    if (detail.isNullOrBlank()) return
    Row(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(labelResId), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = detail)
    }
}

@Preview
@Composable
fun AboutScreenPreview(
) {
    AppTheme {
        AboutScreen(
            appInfo = PlatformAppInfo(
                appId = "com.truepine.photouploader",
                versionName = "1.0.0",
                versionCode = "1"
            )
        )
    }
}