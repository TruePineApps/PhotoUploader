package com.truepineapps.photouploader.model

import com.mohamedrejeb.calf.io.KmpFile
import okio.Path

data class Album(
    val id: String, // Unique identifier (e.g., path)
    val kmpFile: KmpFile? = null, // TODO: Should this be the solution, it must not be optional
    val path: Path,
    val name: String,
    val group: String, // For sticky headers (e.g., parent directory name)
    val photos: List<Photo>,
    val isEnabled: Boolean = true,
)
