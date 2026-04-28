package com.truepineapps.photouploader.core.presentation.navigation

import org.jetbrains.compose.resources.StringResource

/**
 * Interface to describe the navigation destinations for the app
 */
interface NavigationDestination {
    /**
     * Unique name to define the path for a composable
     */
    val route: String

    /**
     * String resource id to that contains title to be displayed for the screen.
     */
    val titleRes: StringResource
}