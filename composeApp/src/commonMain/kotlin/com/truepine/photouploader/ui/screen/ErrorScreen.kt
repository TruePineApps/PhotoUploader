package com.truepine.photouploader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truepine.photouploader.ui.Dimensions
import com.truepine.photouploader.resources.Res
import com.truepine.photouploader.resources.error
import com.truepine.photouploader.resources.retry_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorScreen(message: String, retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.BrokenImage,
            contentDescription = stringResource(Res.string.error),
            modifier = Modifier.size(Dimensions.big_icon_size)
        )
        Text(text = message, modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(Res.string.retry_button))
        }
    }
}