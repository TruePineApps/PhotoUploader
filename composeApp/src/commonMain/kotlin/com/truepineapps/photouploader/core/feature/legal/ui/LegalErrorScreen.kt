package com.truepineapps.photouploader.core.feature.legal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.legal_error_retry
import com.truepineapps.photouploader.resources.legal_error_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LegalErrorScreen(messages: List<UiText>, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.padding_medium),
            modifier = Modifier.padding(Dimensions.padding_medium)
        ) {
            Text(stringResource(Res.string.legal_error_title), style = MaterialTheme.typography.titleMedium)
            
            messages.forEach { message ->
                Text(message.asString(), style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = onRetry) {
                Text(stringResource(Res.string.legal_error_retry))
            }
        }
    }
}