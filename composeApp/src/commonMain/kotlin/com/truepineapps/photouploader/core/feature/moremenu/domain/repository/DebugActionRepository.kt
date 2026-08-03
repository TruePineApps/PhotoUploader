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
