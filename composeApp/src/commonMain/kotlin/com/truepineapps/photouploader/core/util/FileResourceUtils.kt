package com.truepineapps.photouploader.core.util

import com.truepineapps.photouploader.resources.Res

/**
 * Asynchronously loads the content of a resource file located in the "files/" directory.
 *
 * @param fileName The name of the file to be loaded.
 * @return A [Result] containing the trimmed file content as a string on success,
 * or the caught exception on failure.
 */
suspend fun loadResourceFile(fileName: String): Result<String> = runCatching {
    Res.readBytes("files/$fileName").decodeToString().trim()
}

