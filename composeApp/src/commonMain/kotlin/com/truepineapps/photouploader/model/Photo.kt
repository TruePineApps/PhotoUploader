package com.truepineapps.photouploader.model

import com.mohamedrejeb.calf.io.KmpFile
import okio.Path

data class Photo(
    val kmpFile: KmpFile? = null, // TODO: Should this be the solution, it must not be optional
    val path: Path,
    val name: String,
    val isEnabled: Boolean = true
)
