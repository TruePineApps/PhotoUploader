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

package com.truepineapps.photouploader.core.feature.moremenu.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.truepineapps.photouploader.core.feature.moremenu.navigation.MoreMenuNavigator
import com.truepineapps.photouploader.core.presentation.component.ThemedIconButton
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.about
import com.truepineapps.photouploader.resources.debug_actions
import com.truepineapps.photouploader.resources.legal
import com.truepineapps.photouploader.resources.more_menu
import com.truepineapps.photouploader.resources.preferences
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MoreMenu(
    isEnabled: Boolean,
    moreMenuNavigator: MoreMenuNavigator,
    platformInfo: PlatformInfo = koinInject(),
) {
    // The expanded state of the dropdown menu.
    var expanded by remember { mutableStateOf(false) }

    ThemedIconButton(
        imageVector = Icons.Filled.MoreVert,
        contentDescriptionResource = Res.string.more_menu,
        enabled = isEnabled,
        onClick = { expanded = true }
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.preferences)) },
            onClick = {
                moreMenuNavigator.navigateToSettings()
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.about)) },
            onClick = {
                moreMenuNavigator.navigateToAbout()
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.legal)) },
            onClick = {
                moreMenuNavigator.navigateToLegalHub()
                expanded = false
            }
        )
        if (platformInfo.isDebugBuild) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.debug_actions)) },
                onClick = {
                    moreMenuNavigator.navigateToDebugActions()
                    expanded = false
                }
            )
        }
    }
}