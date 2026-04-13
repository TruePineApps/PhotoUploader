/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.data.DataLoadingState
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.loading
import com.truepineapps.photouploader.resources.one_or_more_items
import com.truepineapps.photouploader.resources.unknown_error
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LoadingScreen(
    loadingViewModel: LoadingViewModel,
    modifier: Modifier = Modifier,
    log: Logger = koinInject(),
    successContent: @Composable () -> Unit,
) {
    val name = loadingViewModel.getDisplayValue()
    log.d { "LoadingScreen started with $name" }
    val currentLoadingState by loadingViewModel.loadingState.collectAsState()
    when (currentLoadingState) {
        is DataLoadingState.Loading -> {
            /* Show progress indicator */
            log.d { "LoadingScreen: Loading..." }
            val action = stringResource(Res.string.loading, name)
            ProgressScreen(action = action, modifier = modifier)
        }

        is DataLoadingState.Success -> {
            /* UiState is updated successfully, display data */
            log.d { "LoadingScreen: Loading succeeded, show success" }
            successContent()
        }

        is DataLoadingState.Error -> {
            /* Handle error */
            val message = (currentLoadingState as DataLoadingState.Error).exception.message ?: stringResource(Res.string.unknown_error)
            log.e("LoadingScreen for $name Error: $message")
            ErrorScreen(
                message = message,
                retryAction = { loadingViewModel.reload() },
                modifier = modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Composable
fun LoadingScreen(
    loadingViewModels: List<LoadingViewModel>,
    modifier: Modifier = Modifier,
    log: Logger = koinInject(),
    successContent: @Composable () -> Unit,
) {
    val loadingStatesMap = loadingViewModels.map { vm -> vm to vm.loadingState.collectAsState() }
    if (loadingStatesMap.any { entry -> entry.second.value is DataLoadingState.Loading }) {
        val oneOrMoreItems = stringResource(Res.string.one_or_more_items)
        val action = stringResource(Res.string.loading, oneOrMoreItems)
        ProgressScreen(action = action, modifier = modifier)
    } else if (loadingStatesMap.any { entry -> entry.second.value is DataLoadingState.Error }) {
        val failures = mutableListOf<LoadingViewModel>()
        val messageBuilder = StringBuilder()
        loadingStatesMap.filter { entry -> entry.second.value is DataLoadingState.Error }.forEach { entry ->
            val errorState = entry.second.value as DataLoadingState.Error
            val errorMessage = errorState.exception.message ?: stringResource(Res.string.unknown_error)
            messageBuilder.append("Error for ${errorState::class.simpleName}: $errorMessage\n")
            failures.add(entry.first)
        }
        val message = messageBuilder.toString().trim()
        log.e("LoadingScreen errors:\n$message")
        ErrorScreen(
            message = message,
            retryAction = { failures.forEach { vm -> vm.reload() } },
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    } else {
        successContent()
    }
}

