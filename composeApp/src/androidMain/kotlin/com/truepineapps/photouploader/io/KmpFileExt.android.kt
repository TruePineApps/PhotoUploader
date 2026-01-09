package com.truepineapps.photouploader.io

import android.net.Uri.decode
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import okio.Source
import okio.source
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.FileNotFoundException

/**
 * Lists the contents of this directory.
 *
 * On Android, this utilizes [DocumentFile] to interact with the ContentResolver via the wrapped URI.
 * It assumes the current [KmpFile] represents a directory (specifically a tree URI).
 *
 * @param context The Android [context] required to resolve the URI.
 * @return A list of [KmpFile] objects representing the files and subdirectories contained within this directory.
 *         Returns an empty list if this file is not a directory, cannot be read, or if the URI is invalid.
 */
actual fun KmpFile.list(context: PlatformContext): List<KmpFile> {
    // On Android, PlatformContext is an alias for Context.
    // KmpFile wraps a Uri. We use DocumentFile to traverse it via the ContentResolver.
    val document = DocumentFile.fromTreeUri(context, this.uri) ?: return emptyList()

    if (!document.isDirectory || !document.canRead()) {
        return emptyList()
    }

    // Map the DocumentFile results back to KmpFile (which wraps the new Uri)
    return document.listFiles().map { file ->
        KmpFile(file.uri)
    }
}

/**
 * Converts a KmpFile (wrapping a URI) to an absolute filesystem path string
 * by attempting to map "primary" storage URIs back to raw file paths.
 * Typical value for uri: content://com.android.externalstorage.documents/tree/primary%3AAndroid
 * Typical value for result: /storage/emulated/0/Android
 *
 * @param context not used, standard KmpFile API parameter
 * @return the absolute filesystem path string
 */
actual fun KmpFile.getAbsolutePath(context: PlatformContext): String? = object : KoinComponent {
    val log: Logger by inject()

    fun getPath(): String {
        val uriString = uri.toString()
        val primaryToken = "primary%3A"

        if (uriString.contains(primaryToken)) {
            val relativePath = uriString.substringAfter(primaryToken)
            // Decodes the rest of the path (e.g. converting %2F back to /) if needed,
            val decodedRelativePath = decode(relativePath)

            val externalRoot = Environment.getExternalStorageDirectory().absolutePath
            // Combine them properly
            val result = "$externalRoot/$decodedRelativePath"
            log.d { "getAbsolutePath for $uriString: $result" }
            return result
        }

        // Fallback: if it doesn't match the pattern, return the URI string itself as a Path
        return uriString
    }
}.getPath()

/**
 * Robust check for directory on Android using DocumentFile
 */
actual fun KmpFile.isDir(context: PlatformContext): Boolean {
    val doc = DocumentFile.fromSingleUri(context, uri)
        ?: DocumentFile.fromTreeUri(context, uri)

    // Note: fromSingleUri works for checking isDirectory on a child of a tree
    return doc?.isDirectory == true
}

/**
 * Retrieves the name of the file or directory referenced by this [KmpFile].
 *
 * @param context The Android Context (aliased as [PlatformContext]) required to access the [DocumentFile].
 * @return The display name of the file/directory, or "Unknown" if the name cannot be resolved.
 */
actual fun KmpFile.getDisplayName(context: PlatformContext): String =
    DocumentFile.fromSingleUri(context, uri)?.name
        ?: DocumentFile.fromTreeUri(context, uri)?.name
        ?: "Unknown"

actual fun KmpFile.source(context: PlatformContext): Source {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(this.uri)
        ?: throw FileNotFoundException("Unable to open input stream for URI: $uri")
    return inputStream.source()
}
