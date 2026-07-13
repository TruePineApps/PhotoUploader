package com.truepineapps.photouploader.core.feature.legal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.legal_error_retry
import com.truepineapps.photouploader.resources.legal_error_title
import org.jetbrains.compose.resources.stringResource

@Deprecated("Use ErrorScreen")
@Composable
fun LegalErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.legal_error_title), style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodySmall)
            Button(onClick = onRetry) {
                Text(stringResource(Res.string.legal_error_retry))
            }
        }
    }
}