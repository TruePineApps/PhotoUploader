package com.truepineapps.photouploader.core.feature.moremenu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.core.feature.moremenu.domain.repository.DebugActionRepository
import org.koin.compose.koinInject

@Composable
fun DebugActionScreen(
    modifier: Modifier = Modifier,
    debugRepository: DebugActionRepository = koinInject()
) {
    val debugActions = debugRepository.getActions()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(debugActions) { action ->
            ListItem(
                headlineContent = { Text(action.name) },
                modifier = Modifier.clickable { action.action() }
            )
            HorizontalDivider()
        }
    }
}
