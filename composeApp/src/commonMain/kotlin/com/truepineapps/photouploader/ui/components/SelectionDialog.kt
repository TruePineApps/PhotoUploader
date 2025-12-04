/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.show_selection_dialog
import com.truepineapps.photouploader.ui.Dimensions
import org.jetbrains.compose.resources.stringResource


@Composable
fun <T> SelectionField(
    label: String,
    currentItem: T?,
    onGetItems: () -> List<T>,
    onGetKey: (T) -> Any,
    onGetDisplayName: @Composable (T) -> Any,
    onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = true,
) {
    // String value of the item
    val value = if (currentItem == null) "" else onGetDisplayName(currentItem)

    DialogField(
        label = label,
        value = value,
        modifier = modifier.fillMaxWidth(),
        isRequired = isRequired,
        trailingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(Res.string.show_selection_dialog),
            )
        },
        onShowDialog = { onClose ->
            SelectionDialog(
                label = label,
                currentDisplayValue = value,
                items = onGetItems(),
                onGetKey = onGetKey,
                onGetDisplayName = onGetDisplayName,
                onChange = onChange,
                onDismissRequest = { onClose() },
            )
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionDialog(
    label: String,
    currentDisplayValue: Any,
    items: List<T>,
    onGetKey: (T) -> Any,
    onGetDisplayName: @Composable (T) -> Any,
    onChange: (T) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Defaults equal to DatePickerDialog
    BasicAlertDialog(
        onDismissRequest = onDismissRequest, modifier = modifier.wrapContentHeight()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(
                modifier = Modifier
                    .padding(Dimensions.padding_large)
                    .border(width = Dimensions.border_width, color = MaterialTheme.colorScheme.surfaceTint)
            ) {
                val evenColor = MaterialTheme.colorScheme.surfaceContainer
                val oddColor = MaterialTheme.colorScheme.surfaceVariant
                LazyColumn {
                    item {
                        Text(
                            text = label,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surfaceTint)
                                .padding(Dimensions.padding_small)
                        )
                    }
                    itemsIndexed(
                        items = items, key = { _, item -> onGetKey(item) }
                    ) { index, item ->
                        val displayName = onGetDisplayName(item)
                        val isSelected = displayName == currentDisplayValue
                        val backgroundColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            if (index % 2 == 0) evenColor else oddColor
                        }
                        val textColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color.Unspecified
                        }
                        DisplayableText(
                            text = displayName,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = backgroundColor)
                                .padding(Dimensions.padding_small)
                                .clickable {
                                    onChange(item)
                                    onDismissRequest()
                                })
                    }
                }
            }
        }
    }
}
