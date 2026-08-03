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

package com.truepineapps.photouploader.app.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val lightLowContrastColorScheme by lazy {
    lightColorScheme(
        primary = Color(0xFF425F8B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFACCCFB),
        onPrimaryContainer = Color(0xFF375883),
        inversePrimary = Color(0xFFABC9FA),
        secondary = Color(0xFF545F72),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD6E3F6),
        onSecondaryContainer = Color(0xFF596376),
        tertiary = Color(0xFF7A5917),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEE11F6),
        onTertiaryContainer = Color(0xFF6E510C),
        background = Color(0xFFFBF9FD),
        onBackground = Color(0xFF1A1C1F),
        surface = Color(0xFFFBF9FD),
        onSurface = Color(0xFF1A1C1F),
        surfaceVariant = Color(0xFFE0E2EC),
        onSurfaceVariant = Color(0xFF43474F),
        surfaceTint = Color(0xFF425F8B),
        inverseSurface = Color(0xFF2F3034),
        inverseOnSurface = Color(0xFFF1F0F4),
        error = Color(0xFFBA1A5A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAB2),
        onErrorContainer = Color(0xFF93004A),
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFBF9FD),
        surfaceDim = Color(0xFFDADADD),
        surfaceContainer = Color(0xFFEEEEF1),
        surfaceContainerHigh = Color(0xFFE9E9EC),
        surfaceContainerHighest = Color(0xFFE3E3E6),
        surfaceContainerLow = Color(0xFFF5F3F7),
        surfaceContainerLowest = Color(0xFFFFFFFF),
    )
}

object LightPalette {
    val Primary100 by lazy { Color(0xFFFFFFFF) }
    val Primary95 by lazy { Color(0xFFEBEDFF) }
    val Primary90 by lazy { Color(0xFFD7E3FF) }
    val Primary85 by lazy { Color(0xFFBEC9FF) }
    val Primary80 by lazy { Color(0xFFABC9FA) }
    val Primary75 by lazy { Color(0xFF9DB2EB) }
    val Primary70 by lazy { Color(0xFF8FA1DD) }
    val Primary65 by lazy { Color(0xFF8290CF) }
    val Primary60 by lazy { Color(0xFF7580C1) }
    val Primary55 by lazy { Color(0xFF6870B3) }
    val Primary50 by lazy { Color(0xFF5B61A6) }
    val Primary45 by lazy { Color(0xFF4F5198) }
    val Primary40 by lazy { Color(0xFF425F8B) }
    val Primary35 by lazy { Color(0xFF364B7E) }
    val Primary30 by lazy { Color(0xFF293B72) }
    val Primary25 by lazy { Color(0xFF1C2C66) }
    val Primary20 by lazy { Color(0xFF0E1C5A) }
    val Primary15 by lazy { Color(0xFF000E4E) }
    val Primary10 by lazy { Color(0xFF00033C) }
    val Primary5 by lazy { Color(0xFF000929) }
    val Primary0 by lazy { Color(0xFF000000) }
}

val lightMediumContrastColorScheme by lazy {
    lightColorScheme(
        primary = Color(0xFF16345F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF5173A3),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF2C3948),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF637181),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF4A2F00),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF8A6625),
        onTertiaryContainer = Color(0xFFFFFFFF),
        error = Color(0xFF740136),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFCF2F67),
        onErrorContainer = Color(0xFFFFFFFF),
        background = Color(0xFFFBF9FD),
        onBackground = Color(0xFF1A1C1F),
        surface = Color(0xFFFBF9FD),
        onSurface = Color(0xFF101416),
        surfaceVariant = Color(0xFFE0E2EC),
        onSurfaceVariant = Color(0xFF33373E),
        surfaceTint = Color(0xFF425F8B),
        outline = Color(0xFF4F525A),
        outlineVariant = Color(0xFF6A6C75),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF2F3034),
        inverseOnSurface = Color(0xFFF1F0F4),
        inversePrimary = Color(0xFFABC9FA),
        surfaceDim = Color(0xFFC7C7CA),
        surfaceBright = Color(0xFFFBF9FD),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F3F7),
        surfaceContainer = Color(0xFFE9E9EC),
        surfaceContainerHigh = Color(0xFFDDDEE0),
        surfaceContainerHighest = Color(0xFFD2D2D5),
    )
}

object LightMediumContrastPalette {
    val Primary100 by lazy { Color(0xFFFFFFFF) }
    val Primary95 by lazy { Color(0xFFECF1FF) }
    val Primary90 by lazy { Color(0xFFD7E3FF) }
    val Primary85 by lazy { Color(0xFFBFD9FF) }
    val Primary80 by lazy { Color(0xFFABC9FA) }
    val Primary75 by lazy { Color(0xFF9DB2EC) }
    val Primary70 by lazy { Color(0xFF90A4DD) }
    val Primary65 by lazy { Color(0xFF8393CF) }
    val Primary60 by lazy { Color(0xFF7580C1) }
    val Primary55 by lazy { Color(0xFF6870B3) }
    val Primary50 by lazy { Color(0xFF5C78A6) }
    val Primary45 by lazy { Color(0xFF4F5199) }
    val Primary40 by lazy { Color(0xFF435F8B) }
    val Primary35 by lazy { Color(0xFF364B7F) }
    val Primary30 by lazy { Color(0xFF2A3772) }
    val Primary25 by lazy { Color(0xFF1D2C66) }
    val Primary20 by lazy { Color(0xFF0F1C5A) }
    val Primary15 by lazy { Color(0xFF000D4E) }
    val Primary10 by lazy { Color(0xFF00033C) }
    val Primary5 by lazy { Color(0xFF000929) }
    val Primary0 by lazy { Color(0xFF000000) }
}

val lightHighContrastColorScheme by lazy {
    lightColorScheme(
        primary = Color(0xFF07244D),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF2C4E7B),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF222F3D),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF3F4C5C),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF3D2500),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF624301),
        onTertiaryContainer = Color(0xFFFFFFFF),
        error = Color(0xFF60002A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFF98004A),
        onErrorContainer = Color(0xFFFFFFFF),
        background = Color(0xFFFBF9FD),
        onBackground = Color(0xFF1A1C1F),
        surface = Color(0xFFFBF9FD),
        onSurface = Color(0xFF000000),
        surfaceVariant = Color(0xFFE0E2EC),
        onSurfaceVariant = Color(0xFF000000),
        surfaceTint = Color(0xFF425F8B),
        outline = Color(0xFF292C33),
        outlineVariant = Color(0xFF464951),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF2F3034),
        inverseOnSurface = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFFABC9FA),
        surfaceDim = Color(0xFFB9B9BC),
        surfaceBright = Color(0xFFFBF9FD),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF1F0F4),
        surfaceContainer = Color(0xFFE3E3E6),
        surfaceContainerHigh = Color(0xFFD5D5D8),
        surfaceContainerHighest = Color(0xFFC7C7CA),
    )
}

object LightHighContrastPalette {
    val Primary100 by lazy { Color(0xFFFFFFFF) }
    val Primary95 by lazy { Color(0xFFEBEDFF) }
    val Primary90 by lazy { Color(0xFFD7E3FF) }
    val Primary85 by lazy { Color(0xFFBEC9FF) }
    val Primary80 by lazy { Color(0xFFABC9FA) }
    val Primary75 by lazy { Color(0xFF9DB2EB) }
    val Primary70 by lazy { Color(0xFF8FA1DD) }
    val Primary65 by lazy { Color(0xFF8290CF) }
    val Primary60 by lazy { Color(0xFF7580C1) }
    val Primary55 by lazy { Color(0xFF6870B3) }
    val Primary50 by lazy { Color(0xFF5B60A5) }
    val Primary45 by lazy { Color(0xFF4F5298) }
    val Primary40 by lazy { Color(0xFF425F8B) }
    val Primary35 by lazy { Color(0xFF364B7E) }
    val Primary30 by lazy { Color(0xFF293B72) }
    val Primary25 by lazy { Color(0xFF1C2C65) }
    val Primary20 by lazy { Color(0xFF0E1C5A) }
    val Primary15 by lazy { Color(0xFF000E4E) }
    val Primary10 by lazy { Color(0xFF00033C) }
    val Primary5 by lazy { Color(0xFF000929) }
    val Primary0 by lazy { Color(0xFF000000) }
}

val darkLowContrastLevelColorScheme by lazy {
    darkColorScheme(
        primary = Color(0xFFD7E3FF),
        onPrimary = Color(0xFF0E305A),
        primaryContainer = Color(0xFFACCCFB),
        onPrimaryContainer = Color(0xFF375883),
        secondary = Color(0xFFBCCBE2),
        onSecondary = Color(0xFF273342),
        secondaryContainer = Color(0xFF3F4C5C),
        onSecondaryContainer = Color(0xFFAEBDD4),
        tertiary = Color(0xFFFFE060),
        onTertiary = Color(0xFF422C00),
        tertiaryContainer = Color(0xFFEE11F6),
        onTertiaryContainer = Color(0xFF6E510C),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690035),
        errorContainer = Color(0xFF93004A),
        onErrorContainer = Color(0xFFFFDAB2),
        background = Color(0xFF121316),
        onBackground = Color(0xFFE3E3E6),
        surface = Color(0xFF121316),
        onSurface = Color(0xFFE3E3E6),
        surfaceVariant = Color(0xFF43474F),
        onSurfaceVariant = Color(0xFFC4C6D0),
        surfaceTint = Color(0xFFABC9FA),
        outline = Color(0xFF8D9099),
        outlineVariant = Color(0xFF43474F),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE3E3E6),
        inverseOnSurface = Color(0xFF2F3034),
        inversePrimary = Color(0xFF425F8B),
        surfaceDim = Color(0xFF121316),
        surfaceBright = Color(0xFF38393C),
        surfaceContainerLowest = Color(0xFF0D0E11),
        surfaceContainerLow = Color(0xFF1A1C1F),
        surfaceContainer = Color(0xFF1E2023),
        surfaceContainerHigh = Color(0xFF292A2D),
        surfaceContainerHighest = Color(0xFF333538),
    )
}

object DarkPalette {
    val Primary100 by lazy { Color(0xFFFFFFFF) }
    val Primary95 by lazy { Color(0xFFECF1FF) }
    val Primary90 by lazy { Color(0xFFD6E3FE) }
    val Primary85 by lazy { Color(0xFFC8D4EF) }
    val Primary80 by lazy { Color(0xFFBACBE1) }
    val Primary75 by lazy { Color(0xFFACC0D3) }
    val Primary70 by lazy { Color(0xFF9FB2C5) }
    val Primary65 by lazy { Color(0xFF91A2B7) }
    val Primary60 by lazy { Color(0xFF8493AA) }
    val Primary55 by lazy { Color(0xFF77849C) }
    val Primary50 by lazy { Color(0xFF6B788F) }
    val Primary45 by lazy { Color(0xFF5E6982) }
    val Primary40 by lazy { Color(0xFF525F75) }
    val Primary35 by lazy { Color(0xFF465169) }
    val Primary30 by lazy { Color(0xFF3B435D) }
    val Primary25 by lazy { Color(0xFF2F3651) }
    val Primary20 by lazy { Color(0xFF242945) }
    val Primary15 by lazy { Color(0xFF191C3A) }
    val Primary10 by lazy { Color(0xFF0F1B2F) }
    val Primary5 by lazy { Color(0xFF041124) }
    val Primary0 by lazy { Color(0xFF000000) }
}

val darkMediumContrastColorScheme by lazy {
    darkColorScheme(
        primary = Color(0xFFD7E3FF),
        onPrimary = Color(0xFF052751),
        primaryContainer = Color(0xFFACCCFB),
        onPrimaryContainer = Color(0xFF173862),
        secondary = Color(0xFFD2E0F8),
        onSecondary = Color(0xFF222F3E),
        secondaryContainer = Color(0xFF8795A6),
        onSecondaryContainer = Color(0xFF000000),
        tertiary = Color(0xFFFFE070),
        onTertiary = Color(0xFF3B2800),
        tertiaryContainer = Color(0xFFEE11F6),
        onTertiaryContainer = Color(0xFF4B3400),
        error = Color(0xFFFFD2CC),
        onError = Color(0xFF540028),
        errorContainer = Color(0xFFFF5429),
        onErrorContainer = Color(0xFF000000),
        background = Color(0xFF121316),
        onBackground = Color(0xFFE3E3E6),
        surface = Color(0xFF121316),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF43474F),
        onSurfaceVariant = Color(0xFFDADADD),
        surfaceTint = Color(0xFFABC9FA),
        outline = Color(0xFFAFB2BB),
        outlineVariant = Color(0xFF8D9099),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE3E3E6),
        inverseOnSurface = Color(0xFF292A2D),
        inversePrimary = Color(0xFF2B4D7B),
        surfaceDim = Color(0xFF121316),
        surfaceBright = Color(0xFF444548),
        surfaceContainerLowest = Color(0xFF06070A),
        surfaceContainerLow = Color(0xFF1D1F22),
        surfaceContainer = Color(0xFF212326),
        surfaceContainerHigh = Color(0xFF2C2D31),
        surfaceContainerHighest = Color(0xFF37393C),
    )
}

object DarkMediumContrastPalette {
    val Primary100 by lazy { Color(0xFFFFFFFF) }
    val Primary95 by lazy { Color(0xFFECF1FF) }
    val Primary90 by lazy { Color(0xFFD6E3FE) }
    val Primary85 by lazy { Color(0xFFC8D4EF) }
    val Primary80 by lazy { Color(0xFFBACBE1) }
    val Primary75 by lazy { Color(0xFFACC0D3) }
    val Primary70 by lazy { Color(0xFF9FB2C5) }
    val Primary65 by lazy { Color(0xFF91A2B7) }
    val Primary60 by lazy { Color(0xFF8493AA) }
    val Primary55 by lazy { Color(0xFF77849C) }
    val Primary50 by lazy { Color(0xFF6B788F) }
    val Primary45 by lazy { Color(0xFF5E6982) }
    val Primary40 by lazy { Color(0xFF525F75) }
    val Primary35 by lazy { Color(0xFF465169) }
    val Primary30 by lazy { Color(0xFF3B435D) }
    val Primary25 by lazy { Color(0xFF2F3651) }
    val Primary20 by lazy { Color(0xFF242945) }
    val Primary15 by lazy { Color(0xFF191C3A) }
    val Primary10 by lazy { Color(0xFF0F1B2F) }
    val Primary5 by lazy { Color(0xFF041124) }
    val Primary0 by lazy { Color(0xFF000000) }
}

val darkHighContrastColorScheme by lazy {
    darkColorScheme(
        primary = Color(0xFFEAF1FF),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFFACCCFB),
        onPrimaryContainer = Color(0xFF00152F),
        secondary = Color(0xFFEAF1FF),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFFB8C6DE),
        onSecondaryContainer = Color(0xFF01101E),
        tertiary = Color(0xFFFFEDAD),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFEE11F6),
        onTertiaryContainer = Color(0xFF1C1300),
        error = Color(0xFFFFECE9),
        onError = Color(0xFF000000),
        errorContainer = Color(0xFFFFAEA3),
        onErrorContainer = Color(0xFF22000E),
        background = Color(0xFF121316),
        onBackground = Color(0xFFE3E3E6),
        surface = Color(0xFF121316),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF43474F),
        onSurfaceVariant = Color(0xFFFFFFFF),
        surfaceTint = Color(0xFFABC9FA),
        outline = Color(0xFFEDEFF9),
        outlineVariant = Color(0xFFC0C2CC),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE3E3E6),
        inverseOnSurface = Color(0xFF000000),
        inversePrimary = Color(0xFF2B4D7B),
        surfaceDim = Color(0xFF121316),
        surfaceBright = Color(0xFF4F5054),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF1E2023),
        surfaceContainer = Color(0xFF2F3034),
        surfaceContainerHigh = Color(0xFF3A3B3F),
        surfaceContainerHighest = Color(0xFF46474A),
    )
}

object DarkHighContrastPalette {
    val Primary100 by lazy { Color(0xFFFFFFFF) }
    val Primary95 by lazy { Color(0xFFEBEDFF) }
    val Primary90 by lazy { Color(0xFFDCE2F1) }
    val Primary85 by lazy { Color(0xFFCED3E3) }
    val Primary80 by lazy { Color(0xFFC0C7D5) }
    val Primary75 by lazy { Color(0xFFB3BAC7) }
    val Primary70 by lazy { Color(0xFFA5ACB9) }
    val Primary65 by lazy { Color(0xFF989FAC) }
    val Primary60 by lazy { Color(0xFF8A919E) }
    val Primary55 by lazy { Color(0xFF7D8691) }
    val Primary50 by lazy { Color(0xFF717784) }
    val Primary45 by lazy { Color(0xFF646B77) }
    val Primary40 by lazy { Color(0xFF585F6B) }
    val Primary35 by lazy { Color(0xFF4C535F) }
    val Primary30 by lazy { Color(0xFF404753) }
    val Primary25 by lazy { Color(0xFF353B47) }
    val Primary20 by lazy { Color(0xFF2A2D3C) }
    val Primary15 by lazy { Color(0xFF202331) }
    val Primary10 by lazy { Color(0xFF151C26) }
    val Primary5 by lazy { Color(0xFF0A111B) }
    val Primary0 by lazy { Color(0xFF000000) }
}

object StatusPalette {
    val Success = Color(0xFF81DB7B)
    val Warning = Color(0xFFEA983D)
    val Error = Color(0xFFF76442)
    val Disabled = Color(0xFF787878)
}