package com.truepineapps.photouploader.ui.components

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
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.util.Opacity
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