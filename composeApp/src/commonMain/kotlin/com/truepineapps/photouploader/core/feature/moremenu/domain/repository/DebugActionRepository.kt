package com.truepineapps.photouploader.core.feature.moremenu.domain.repository

import com.truepineapps.photouploader.core.feature.moremenu.domain.model.DebugAction

/*
To expand with more DebugActions:
1. Generic Actions (Core): Add new DebugAction objects in the "core_actions" list in MoreMenuModule.
2. Feature-Specific Actions (Feature Tier): If feature/<app>> needs a DebugAction, define a Koin
module in feature/<app>/di/<app>Module.kt providing a List<DebugAction> (e.g., named
"<app>_actions") similar to the "core_actions" list in MoreMenuModule.
The Koin instance of DebugActionRepository flattens all injected lists to collect all DebugActions.
 */

interface DebugActionRepository {
    fun getActions(): List<DebugAction>
}
