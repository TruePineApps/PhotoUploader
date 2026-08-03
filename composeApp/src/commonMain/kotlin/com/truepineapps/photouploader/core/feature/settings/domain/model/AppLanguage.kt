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

package com.truepineapps.photouploader.core.feature.settings.domain.model

import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.en
import com.truepineapps.photouploader.resources.nl
import com.truepineapps.photouploader.resources.system
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(
    val code: String,
    val stringRes: StringResource
) {
    System(DEFAULT_LOCALE_FROM_PLATFORM, Res.string.system),
    English("en", Res.string.en),
    Dutch("nl", Res.string.nl)
}