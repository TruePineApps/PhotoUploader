package com.truepine.photouploader.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.truepine.photouploader.data.preferences.DEFAULT_LOCALE
import com.truepine.photouploader.ui.screen.LoadingScreen

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
    content: @Composable () -> Unit
) {
    println("Starting AppEnvironment")
    LoadingScreen(loadingViewModel = localeViewModel, modifier = modifier) {

        // Collect the effective locale tag (e.g., "en-US") from the ViewModel's StateFlow.
        // This triggers recomposition whenever effectiveAppLocale emits a new value.
        val effectiveLocaleTag = localeViewModel.preferredLocaleState.collectAsState().value
        println("AppEnvironment : effectiveLocaleTag=$effectiveLocaleTag")

        // Provide this effectiveLocaleTag via the LocalAppLocale CompositionLocal.
        // This allows stringResource, painterResource, etc., from Compose Multiplatform
        // resource libraries to pick up the correct language.
        CompositionLocalProvider(
            LocalAppLocale provides effectiveLocaleTag
        ) {
            key(effectiveLocaleTag) {
                content()
            }
        }
    }
}
