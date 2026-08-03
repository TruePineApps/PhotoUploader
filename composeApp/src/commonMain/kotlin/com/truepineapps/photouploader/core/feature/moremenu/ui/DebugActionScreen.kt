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

package com.truepineapps.photouploader.core.feature.moremenu.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.moremenu.domain.repository.DebugActionRepository
import com.truepineapps.photouploader.core.presentation.component.SelectionDialogContent
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.debug_actions
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Don't bother with translations for debug actions
@Composable
fun DebugActionScreen(
    modifier: Modifier = Modifier,
    debugRepository: DebugActionRepository = koinInject(),
    log: Logger = koinInject()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier.fillMaxSize()) {
        SelectionDialogContent(
            label = stringResource(Res.string.debug_actions),
            currentDisplayValue = "",
            items = debugRepository.getActions(),
            onGetKey = { it.name },
            onGetDisplayName = { it.name },
            onChange = { action ->
                try {
                    action.action()
                } catch (e: Exception) {
                    log.e("Error while executing debug action ${action.name}", e)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Error while executing debug action ${action.name}: ${e.message ?: "Unknown error"}",
                            duration = SnackbarDuration.Long,
                            withDismissAction = true
                        )
                    }
                }
            },
            onDismissRequest = { },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            // Custom Snackbar with error styling
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                actionColor = MaterialTheme.colorScheme.onError
            )
        }
    }
}
