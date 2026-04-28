package com.truepineapps.photouploader.core.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType

fun NavBackStackEntry.getStringArg(key: String): String? {
    return arguments?.let { NavType.StringType[it, key] }
}