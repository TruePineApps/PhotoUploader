package com.truepineapps.photouploader.feature.uploader.domain.repository

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.domain.repository.DataLoadingRepository
import com.truepineapps.photouploader.feature.uploader.domain.model.Album
import kotlinx.coroutines.flow.StateFlow

interface PhotoDirectoryRepository : DataLoadingRepository {
    val albums: StateFlow<List<Album>>
    fun setPath(kmpFile: KmpFile, platformContext: PlatformContext)

    // Allow updating albums from outside (e.g. toggling check boxes in ViewModel)
    fun updateAlbums(newAlbums: List<Album>)
}