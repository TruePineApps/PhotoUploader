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

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/** Input field that shows a string representation of a complex value that is set through a dialog */
@Composable
fun DialogField(
    label: String,
    value: Any,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    onShowDialog: @Composable (onClose: () -> Unit) -> Unit = {},
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val interactionSource = remember {
        object : MutableInteractionSource {
            override val interactions = MutableSharedFlow<Interaction>(
                extraBufferCapacity = 16,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

            override suspend fun emit(interaction: Interaction) {
                when (interaction) {
                    is PressInteraction.Press -> {
                        showDialog = true
                    }

                    is PressInteraction.Release -> {
                        showDialog = true
                    }

                    is FocusInteraction.Focus -> {
                        showDialog = true
                    }

                    is DragInteraction.Start -> {
                        showDialog = true
                    }
                }

                // No interaction, no interactions.emit(interaction)
            }

            override fun tryEmit(interaction: Interaction): Boolean {
                // No interaction: No return interactions.tryEmit(interaction)
                return false
            }
        }
    }

    var colors = OutlinedTextFieldDefaults.colors()
    if (isRequired) {
        colors = colors.copy(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    }
    if (value is AnnotatedString) {
        OutlinedTextField(
            value = TextFieldValue(value),
            onValueChange = { },
            label = { Text(label) },
            colors = colors,
            trailingIcon = trailingIcon,
            readOnly = true,
            singleLine = true,
            interactionSource = interactionSource,
            keyboardActions = KeyboardActions(onDone = { showDialog = true }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = modifier
        )
    } else {
        OutlinedTextField(
            value = value as? String ?: value.toString(),
            onValueChange = { },
            label = { Text(label) },
            colors = colors,
            trailingIcon = trailingIcon,
            readOnly = true,
            singleLine = true,
            interactionSource = interactionSource,
            keyboardActions = KeyboardActions(onDone = { showDialog = true }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = modifier
        )
    }
    if (showDialog) {
        onShowDialog { showDialog = false }
    }
}
