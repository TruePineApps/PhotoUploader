/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.ui.screen.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.core.util.PlatformType
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.about
import com.truepineapps.photouploader.resources.about_photo_uploader
import com.truepineapps.photouploader.resources.app_id
import com.truepineapps.photouploader.resources.photo_uploader_description
import com.truepineapps.photouploader.resources.platform
import com.truepineapps.photouploader.resources.sources
import com.truepineapps.photouploader.resources.version
import com.truepineapps.photouploader.resources.website
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.ui.navigation.NavigationDestination
import com.truepineapps.photouploader.ui.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
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
            text = stringResource(Res.string.about_photo_uploader) + "(Beta)",
            style = MaterialTheme.typography.titleLarge,
        )
        PlatformDetailRow(Res.string.version, "${appInfo.versionName} (${appInfo.versionCode})")
        PlatformDetailRow(Res.string.app_id, appInfo.appId)
        PlatformDetailRow(Res.string.platform, platformInfo.name)
        PlatformDetailRow(Res.string.website, "truepineapps.com/photouploader")
        PlatformDetailRow(Res.string.sources, "github.com/truepineapps/photouploader")
        Spacer(modifier = Modifier.height(Dimensions.padding_medium))
        Text(
            stringResource(Res.string.photo_uploader_description),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.padding_small)
        )
    }
}

@Composable
private fun PlatformDetailRow(
    labelResId: StringResource, detail: String?, modifier: Modifier = Modifier,
) {
    if (detail.isNullOrBlank()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.padding_small)
    ) {
        Text(stringResource(labelResId), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = detail)
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun AboutScreenPreview(
) {
    AppTheme {
        AboutScreen(
            appInfo = object : AppInfo {
                override val appId = "com.truepineapps.photouploader"
                override val versionName = "1.0.0"
                override val versionCode = "1"
            },
            platformInfo = object : PlatformInfo {
                override val name = "JVM"
                override val platformType = PlatformType.NATIVE
                override val isDebugBuild = true
            }
        )
    }
}