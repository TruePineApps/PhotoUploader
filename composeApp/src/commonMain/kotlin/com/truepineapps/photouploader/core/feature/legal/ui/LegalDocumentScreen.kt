/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.feature.legal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalUiState
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalViewModel
import com.truepineapps.photouploader.core.presentation.component.MarkDownText
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.loading
import com.truepineapps.photouploader.resources.privacy_policy
import com.truepineapps.photouploader.resources.terms_of_service
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

enum class DocumentType(val titleRes: StringResource) {
    TERMS(Res.string.terms_of_service),
    PRIVACY(Res.string.privacy_policy)
}

@Composable
fun LegalDocumentScreen(
    type: DocumentType,
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    log: Logger,
    modifier: Modifier = Modifier,
    viewModel: LegalViewModel = koinViewModel(),
) {
    val context = LocalPlatformContext.current
    val uiState by viewModel.state.collectAsState()

    onUpdateTopAppBar(stringResource(type.titleRes), null) {}

    LaunchedEffect(type) {
        viewModel.loadDocument(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is LegalUiState.Loading -> {
                Text(
                    text = stringResource(Res.string.loading, stringResource(type.titleRes)),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is LegalUiState.Error -> {
                LegalErrorScreen(
                    messages = state.messages,
                    onRetry = { viewModel.loadDocument(context) }
                )
            }
            is LegalUiState.DocumentLoaded -> {
                val markdownText = when (type) {
                    DocumentType.TERMS -> state.content.termsOfService
                    DocumentType.PRIVACY -> state.content.privacyPolicy
                }
                MarkDownText(
                    markdown = markdownText,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.padding_medium)
                        .verticalScroll(rememberScrollState())
                )
            }
            else -> {
                // ShowLegal and Accepted are never returned by viewModel.loadDocument
                log.e("Unexpected state: $state")
            }
        }
    }
}
