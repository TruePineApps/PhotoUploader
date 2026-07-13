package com.truepineapps.photouploader.core.feature.legal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalIntent
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalUiState
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Drop-in gate composable. Renders [appContent] only when legal acceptance
 * is confirmed; otherwise shows loading, error, or the legal flow.
 */
@Composable
fun LegalGateScreen(
    viewModel: LegalViewModel = koinViewModel(),
    appContent: @Composable () -> Unit,
) {
    val context = LocalPlatformContext.current

    // By using LaunchedEffect(Unit), the acceptance check runs once per composition lifecycle. This
    // initializes the legal state; any subsequent state transitions are purely user-initiated via
    // ViewModel intents.
    LaunchedEffect(Unit) {
        viewModel.checkAcceptance(context)
    }


    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is LegalUiState.Loading -> LegalLoadingScreen()
        is LegalUiState.Error -> LegalErrorScreen(
            message = s.message,
            onRetry = { viewModel.onIntent(context, LegalIntent.Retry) }
        )

        is LegalUiState.ShowLegal -> LegalConsentScreen(
            state = s,
            onIntent = { viewModel.onIntent(context, it) }
        )

        is LegalUiState.Accepted -> appContent()
    }
}