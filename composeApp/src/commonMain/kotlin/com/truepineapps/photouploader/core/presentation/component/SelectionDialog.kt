/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.presentation.component

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
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.show_selection_dialog
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
        SelectionDialogContent(
            label = label,
            currentDisplayValue = currentDisplayValue,
            items = items,
            onGetKey = onGetKey,
            onGetDisplayName = onGetDisplayName,
            onChange = onChange,
            onDismissRequest = onDismissRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionDialogContent(
    label: String,
    currentDisplayValue: Any,
    items: List<T>,
    onGetKey: (T) -> Any,
    onGetDisplayName: @Composable (T) -> Any,
    onChange: (T) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(
            modifier = Modifier
                .padding(Dimensions.padding_large)
                .border(
                    width = Dimensions.border_width,
                    color = MaterialTheme.colorScheme.surfaceTint
                )
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

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewSelectionDialogContent() {
    // Note that the preview shows as if the mouse hovers "value 1"
    AppTheme {
        SelectionDialogContent(
            label = "Preview",
            currentDisplayValue = "value 3",
            items = listOf("value 1", "value 2", "value 3"),
            onGetKey = { it },
            onGetDisplayName = { it },
            onChange = { },
            onDismissRequest = { },
        )
    }
}
