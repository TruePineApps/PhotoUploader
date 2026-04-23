/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Text input field styled for this app, defaulting to a non-required, single line field.
 * @param labelId The string resource for the label.
 * @param value The current value of the field.
 * @param onValueChange The callback to invoke when the value changes.
 * @param modifier The modifier to apply to the field.
 * @param trailingIcon The optional trailing icon to display.
 * @param singleLine Whether the field should be single-line.
 * @param isRequired Whether the field is required.
 */
@Composable
fun InputField(
    labelId: StringResource,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    isRequired: Boolean = false,
) {
    var colors = OutlinedTextFieldDefaults.colors()
    if (isRequired) {
        colors = colors.copy(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelId)) },
        colors = colors,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        modifier = modifier
    )
}

/** Single line boolean input field styled for this app.
 * @param labelId The string resource for the label.
 * @param value The current value of the field.
 * @param onValueChange The callback to invoke when the value changes.
 * @param modifier The modifier to apply to the field.
 * @param isSwitch Whether to use a switch instead of a checkbox.
 */
@Composable
fun BooleanInputField(
    labelId: StringResource,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isSwitch: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (isSwitch) {
            Text(stringResource(labelId))
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = value, onCheckedChange = onValueChange)
        } else {
            Checkbox(checked = value, onCheckedChange = onValueChange)
            Spacer(Modifier.width(Dimensions.padding_very_small))
            Text(stringResource(labelId))
        }
    }
}

