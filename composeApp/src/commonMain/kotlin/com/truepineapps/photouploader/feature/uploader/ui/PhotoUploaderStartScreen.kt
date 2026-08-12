package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import com.truepineapps.photouploader.core.presentation.component.ThemedIconButton
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.util.normalizeWhitespace
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.disclosure_summary
import com.truepineapps.photouploader.resources.select_photo_folder
import org.jetbrains.compose.resources.stringResource

@Composable
fun StartScreen(
    showDirPicker: () -> Unit,
    canChooseDirectory: Boolean,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        // --- Pass 1: probe the *natural* height of the content (icon block + disclosure text) ---
        val probeConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val probePlaceables = subcompose("probe") {
            StartScreenContent(
                showDirPicker = showDirPicker,
                canChooseDirectory = canChooseDirectory,
                // measure it "naturally"
                isScrollable = false,
                isCalculation = true
            )
        }.map { it.measure(probeConstraints) }

        val naturalHeight = probePlaceables.maxOfOrNull { it.height } ?: 0
        val needsScroll = naturalHeight > constraints.maxHeight

        // --- Pass 2: build the real UI, switching strategy based on the probe result ---
        val contentPlaceables = subcompose("content") {
            StartScreenContent(
                showDirPicker = showDirPicker,
                canChooseDirectory = canChooseDirectory,
                isScrollable = needsScroll,
                isCalculation = false
            )
        }.map { it.measure(constraints) }

        val width = contentPlaceables.maxOfOrNull { it.width } ?: constraints.maxWidth

        layout(width, constraints.maxHeight) {
            contentPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}

@Composable
private fun StartScreenContent(
    showDirPicker: () -> Unit,
    canChooseDirectory: Boolean,
    isScrollable: Boolean = false,
    isCalculation: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val columnModifier = if (isScrollable && !isCalculation) {
        // Not enough room: allow scrolling.
        modifier.fillMaxWidth().verticalScroll(scrollState)
    } else {
        // Enough room: fill available height so the weighted Box can center the icon.
        modifier.fillMaxSize()
    }

    Column(modifier = columnModifier) {
        Box(
            modifier = if (isScrollable || isCalculation) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.weight(1f).fillMaxWidth()
            },
            contentAlignment = Alignment.Center
        ) {
            IconBlock(showDirPicker, canChooseDirectory)
        }

        DisclosureText()
    }
}

@Composable
private fun IconBlock(showDirPicker: () -> Unit, canChooseDirectory: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ThemedIconButton(
            imageVector = Icons.Filled.PermMedia,
            contentDescriptionResource = Res.string.select_photo_folder,
            iconSize = Dimensions.big_icon_size,
            enabled = canChooseDirectory,
            onClick = showDirPicker,
        )
        Text(
            text = stringResource(Res.string.select_photo_folder),
            modifier = Modifier.padding(Dimensions.padding_medium)
        )
    }
}

@Composable
private fun DisclosureText() {
    Text(
        text = stringResource(Res.string.disclosure_summary).normalizeWhitespace(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.padding_large)
            .padding(bottom = Dimensions.padding_medium),
        textAlign = TextAlign.Center
    )
}