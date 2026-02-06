package com.truepineapps.photouploader.ui.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun isCompactWidth (width: Dp) : Boolean = width < 600.dp
fun isMediumWidth (width: Dp) : Boolean = width >= 600.dp && width < 840.dp
fun isExpandedWidth (width: Dp) : Boolean = width >= 840.dp

fun isCompactHeight (height: Dp) : Boolean = height < 480.dp
fun isMediumHeight (height: Dp) : Boolean = height >= 480.dp && height < 900.dp
fun isExpandedHeight (height: Dp) : Boolean = height >= 900.dp