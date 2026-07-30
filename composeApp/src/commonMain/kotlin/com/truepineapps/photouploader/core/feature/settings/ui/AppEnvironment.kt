package com.truepineapps.photouploader.core.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.feature.settings.domain.model.DEFAULT_LOCALE
import com.truepineapps.photouploader.core.presentation.component.LoadingScreen
import com.truepineapps.photouploader.core.feature.settings.viewmodel.LocaleViewModel
import org.koin.compose.koinInject

/**
 * CompositionLocal to provide the current effective BCP 47 locale tag (e.g., "en-US")
 * to the rest of the UI tree.
 * Compose Multiplatform resource libraries look for such a local to determine
 * which language's resources to load.
 * If no locale is set in preferences or on the platform, the default is English; use this as the
 * initialization value for the effective LocalAppLocale.
 */
val LocalAppLocale = staticCompositionLocalOf { DEFAULT_LOCALE }

@Composable
fun AppEnvironment(
    modifier: Modifier = Modifier,
    localeViewModel: LocaleViewModel,
    log: Logger = koinInject(),
    content: @Composable () -> Unit
) {
    log.d { "Starting AppEnvironment" }
    LoadingScreen(loadingViewModel = localeViewModel, log = log, modifier = modifier) {

        // Collect the effective locale state from the ViewModel's StateFlow.
        // This triggers recomposition whenever the state (tag or selection) changes.
        val localeState = localeViewModel.preferredLocaleState.collectAsState().value
        val effectiveLocaleTag = localeState.currentTag
        
        log.d { "AppEnvironment : effectiveLocaleTag=$effectiveLocaleTag, selection=${localeState.selectedTag}" }

        // Provide this effectiveLocaleTag via the LocalAppLocale CompositionLocal.
        // This allows stringResource, painterResource, etc., from Compose Multiplatform
        // resource libraries to pick up the correct language.
        CompositionLocalProvider(
            LocalAppLocale provides effectiveLocaleTag
        ) {
            // We use the combined state as key to ensure that even if the tag is the same,
            // a change in selection (e.g. Dutch -> System) forces a recomposition.
            key(localeState) {
                content()
            }
        }
    }
}
