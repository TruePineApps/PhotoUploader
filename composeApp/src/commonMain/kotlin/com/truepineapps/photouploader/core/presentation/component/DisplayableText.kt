package com.truepineapps.photouploader.core.presentation.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

@Composable
fun DisplayableText(
    text: Any,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    if (text is AnnotatedString) {
        Text(text = text, color = color, modifier = modifier)
    } else {
        Text(
            text = text as? String ?: text.toString(),
            color = color,
            modifier = modifier
        )
    }
}