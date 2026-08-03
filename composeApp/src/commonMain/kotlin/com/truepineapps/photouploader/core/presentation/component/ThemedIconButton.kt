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

package com.truepineapps.photouploader.core.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.design.Opacity
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemedIconButton(
    imageVector: ImageVector,
    contentDescriptionResource: StringResource,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = Dimensions.icon_size,
) {
    val foregroundColor = MaterialTheme.colorScheme.primary
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonColors(
            containerColor = Color.Transparent,
            contentColor = foregroundColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = foregroundColor.copy(alpha = Opacity.DISABLED.value)
        ),
        modifier = modifier.size(Dp(iconSize.value * 1.5f))
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(contentDescriptionResource),
            modifier = Modifier.size(iconSize)
        )
    }
}