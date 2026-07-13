package com.truepineapps.photouploader.core.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText

@OptIn(ExperimentalRichTextApi::class)
@Composable
fun MarkDownText(markdown: String, modifier: Modifier = Modifier) {
    val state = rememberRichTextState()
    state.setMarkdown(markdown)

    RichText(
        state = state,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
