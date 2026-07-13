package com.truepineapps.photouploader.core.feature.legal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.legal_loading
import org.jetbrains.compose.resources.stringResource

@Deprecated("Use LoadingViewModel")
@Composable
fun LegalLoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(stringResource(Res.string.legal_loading))
        }
    }
}