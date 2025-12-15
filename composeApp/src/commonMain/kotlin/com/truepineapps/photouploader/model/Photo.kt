package com.truepineapps.photouploader.model

import com.mohamedrejeb.calf.io.KmpFile
import okio.Path

data class Photo(
    val kmpFile: KmpFile,
    val path: Path,
    val name: String,
    val isEnabled: Boolean = true
)
