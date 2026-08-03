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

package com.truepineapps.photouploader.core.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun isCompactWidth (width: Dp) : Boolean = width < 600.dp
fun isMediumWidth (width: Dp) : Boolean = width >= 600.dp && width < 840.dp
fun isExpandedWidth (width: Dp) : Boolean = width >= 840.dp

fun isCompactHeight (height: Dp) : Boolean = height < 480.dp
fun isMediumHeight (height: Dp) : Boolean = height >= 480.dp && height < 900.dp
fun isExpandedHeight (height: Dp) : Boolean = height >= 900.dp