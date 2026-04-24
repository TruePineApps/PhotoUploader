/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.language
import com.truepineapps.photouploader.resources.preferences
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.component.SelectionField
import com.truepineapps.photouploader.core.feature.settings.domain.model.AppLanguage
import com.truepineapps.photouploader.app.navigation.NavigationDestination
import com.truepineapps.photouploader.core.presentation.component.LoadingScreen
import com.truepineapps.photouploader.core.feature.settings.viewmodel.SettingsViewModel
import com.truepineapps.photouploader.app.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = Res.string.preferences
}

@Composable
fun SettingsScreen(
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    modifier: Modifier = Modifier,
    log: Logger = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject()
) {
    onUpdateTopAppBar(stringResource(SettingsDestination.titleRes), null) {}

    LoadingScreen(loadingViewModel = settingsViewModel, log = log) {
        val settingsUiState by settingsViewModel.settingsUiState.collectAsState()

        SettingsBody(
            language = AppLanguage.entries.firstOrNull { settingsUiState.localeTag.startsWith(it.code) }
                ?: AppLanguage.System,
            setLanguage = settingsViewModel::setLocale,
            modifier = modifier.padding(Dimensions.padding_small)
        )
    }
}

@Composable
fun SettingsBody(
    language: AppLanguage,
    setLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        SelectionField(
            label = stringResource(Res.string.language),
            currentItem = language,
            onGetItems = { AppLanguage.entries },
            onGetKey = { it.ordinal },
            onGetDisplayName = { stringResource(it.stringRes) },
            onChange = { setLanguage(it.code) },
            modifier = Modifier.fillMaxWidth(),
            isRequired = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        SettingsBody(
            language = AppLanguage.English,
            setLanguage = {},
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // showBackground = true
        )
    }
}