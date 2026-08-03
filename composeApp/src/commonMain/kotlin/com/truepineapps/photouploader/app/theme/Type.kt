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

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.truepineapps.photouploader.resources.NotoSans_Bold
import com.truepineapps.photouploader.resources.NotoSans_BoldItalic
import com.truepineapps.photouploader.resources.NotoSans_Italic
import com.truepineapps.photouploader.resources.NotoSans_Medium
import com.truepineapps.photouploader.resources.NotoSans_MediumItalic
import com.truepineapps.photouploader.resources.NotoSans_Regular
import com.truepineapps.photouploader.resources.Res
import org.jetbrains.compose.resources.Font

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun getAppTypography(): Typography {
    val notoSans = getNotoSansFontFamily()

    return Typography(
        // 1. Titles (Presentation)
        displayLarge = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        displayMedium = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        displaySmall = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),

        // 2. Subtitles / Headlines
        headlineLarge = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),

        // 3. Headings
        titleLarge = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,      // Primary Heading size
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),

        // 4. Body Text
        bodyLarge = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // 5. Buttons (Label Large)
        labelLarge = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),

        // 6. Small Labels / Captions
        labelSmall = TextStyle(
            fontFamily = notoSans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

@Composable
fun getNotoSansFontFamily(): FontFamily {
    return FontFamily(
        Font(resource = Res.font.NotoSans_Regular, weight = FontWeight.Normal),
        Font(resource = Res.font.NotoSans_Medium, weight = FontWeight.Medium),
        Font(resource = Res.font.NotoSans_Bold, weight = FontWeight.Bold),
        Font(resource = Res.font.NotoSans_Italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(resource = Res.font.NotoSans_MediumItalic, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(resource = Res.font.NotoSans_BoldItalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    )
}
