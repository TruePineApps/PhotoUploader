package com.truepineapps.photouploader.feature.uploader.domain.model

import com.mohamedrejeb.calf.io.KmpFile
import okio.Path

data class Photo(
    val kmpFile: KmpFile,
    val path: Path,
    /** File name */
    val name: String,
) {
    /** File name without extension */
    fun getDisplayName(): String {
        // Safe filename parsing
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) name.take(dotIndex) else name
    }
}
