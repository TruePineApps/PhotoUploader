/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.feature.moremenu.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.core.feature.moremenu.navigation.AboutDestination
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.core.util.PlatformType
import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.core.util.UiTextString
import com.truepineapps.photouploader.core.util.normalizeWhitespace
import com.truepineapps.photouploader.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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
            .padding(Dimensions.padding_medium)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(Res.string.about_photo_uploader) + " (Beta)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimensions.padding_medium),
        )

        Text(
            text = stringResource(Res.string.photo_uploader_description).normalizeWhitespace(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = Dimensions.padding_large)
        )

        // ── Application ───────────────────────────────────────────────
        SectionHeader(Res.string.application_info)
        DetailRow(Res.string.version, "${appInfo.versionName} (${appInfo.versionCode})")
        DetailRow(Res.string.app_id, appInfo.appId)
        DetailRow(Res.string.website, "truepineapps.com/photouploader")
        DetailRow(Res.string.sources, "github.com/truepineapps/photouploader")

        Spacer(modifier = Modifier.height(Dimensions.padding_large))

        // ── Build Configuration ───────────────────────────────────────
        SectionHeader(Res.string.build_configuration)
        DetailRow(
            Res.string.build_type,
            stringResource(if (platformInfo.isDebugBuild) Res.string.debug else Res.string.release),
        )
        DetailRow(Res.string.build_target, appInfo.targetInfo.asString())

        Spacer(modifier = Modifier.height(Dimensions.padding_large))

        // ── System Environment ────────────────────────────────────────
        SectionHeader(Res.string.system_environment)
        DetailRow(Res.string.platform, platformInfo.name.asString())
        DetailRow(Res.string.os_name, platformInfo.osName)
        DetailRow(Res.string.os_version, platformInfo.osVersion)
        DetailRow(Res.string.kernel_version, platformInfo.kernelVersion)
        DetailRow(Res.string.cpu_architecture, platformInfo.cpuArch.asString())
        DetailRow(
            Res.string.runtime,
            "${platformInfo.runtimeName} ${platformInfo.runtimeVersion.asString()}"
        )
        DetailRow(Res.string.processors, platformInfo.availableProcessors.asString())
        DetailRow(Res.string.memory, platformInfo.maxMemory?.asString())
    }
}

@Composable
private fun SectionHeader(labelResId: StringResource) {
    Column {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = Dimensions.padding_small),
        )
        HorizontalDivider(
            modifier = Modifier.padding(bottom = Dimensions.padding_small),
            thickness = Dimensions.border_width,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun DetailRow(
    labelResId: StringResource,
    detail: String?,
    modifier: Modifier = Modifier,
) {
    if (detail.isNullOrBlank()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.padding_very_small)
    ) {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun AboutScreenAndroidPreview() {
    AboutScreen(
        appInfo = object : AppInfo {
            override val appId = "com.truepineapps.photouploader"
            override val versionName = "1.0.0"
            override val versionCode = "1"
            override val targetInfo = UiTextString("Target SDK 37")
        },
        platformInfo = object : PlatformInfo {
            override val name = UiTextString("Android 14 (API 34)")
            override val osName: String? = null
            override val osVersion: String? = null
            override val kernelVersion: String = "6.1.0-generic"
            override val cpuArch: UiText = UiTextString("x86_64")
            override val availableProcessors = UiTextString("8")
            override val maxMemory = UiTextString("4.0 GB")
            override val runtimeName: String = "ART"
            override val runtimeVersion: UiText = UiTextString("2.1.0")
            override val platformType = PlatformType.NATIVE
            override val isDebugBuild = true
        }
    )
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun AboutScreenJvmPreview() {
    AboutScreen(
        appInfo = object : AppInfo {
            override val appId = "com.truepineapps.photouploader"
            override val versionName = "1.0.0"
            override val versionCode = "1"
            override val targetInfo = UiTextString("Target JVM 11")
        },
        platformInfo = object : PlatformInfo {
            override val name = UiTextString("Ubuntu 24.04.4 LTS")
            override val osName: String = "Linux"
            override val osVersion: String? = null
            override val kernelVersion: String = "7.0.0-28-generic"
            override val cpuArch: UiText = UiTextString("amd64")
            override val availableProcessors = UiTextString("16")
            override val maxMemory = UiTextString("3.7 GB")
            override val runtimeName: String = "Java"
            override val runtimeVersion: UiText = UiTextString("21.0.11")
            override val platformType = PlatformType.NATIVE
            override val isDebugBuild = true
        }
    )
}
