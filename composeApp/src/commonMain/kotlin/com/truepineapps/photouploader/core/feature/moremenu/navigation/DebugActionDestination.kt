package com.truepineapps.photouploader.core.feature.moremenu.navigation

import com.truepineapps.photouploader.core.presentation.navigation.NavigationDestination
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.debug_actions

object DebugActionDestination : NavigationDestination {
    override val route = "debug_actions"
    override val titleRes = Res.string.debug_actions
}
