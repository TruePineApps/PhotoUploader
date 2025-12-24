package com.truepineapps.photouploader.model

import com.mohamedrejeb.calf.io.KmpFile
import okio.Path

data class Photo(
    val kmpFile: KmpFile,
    val path: Path,
    /** File name */
    val name: String,
    val isEnabled: Boolean = true,
    val isCoverPhoto: Boolean = false,
    /** Google Photo ID after upload */
    val mediaItemId: String? = null,
    val uploadStatus: UploadStatus = UploadStatus.None,
) {
    /** File name without extension */
    fun getDisplayName(): String {
        // Safe filename parsing
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) name.take(dotIndex) else name
    }
}
