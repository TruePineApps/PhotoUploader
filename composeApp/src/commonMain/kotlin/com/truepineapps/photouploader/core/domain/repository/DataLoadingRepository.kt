package com.truepineapps.photouploader.core.domain.repository

import com.truepineapps.photouploader.core.domain.state.DataLoadingState
import kotlinx.coroutines.flow.Flow

interface DataLoadingRepository {
    /** Return a flow that keeps the loading state of the data */
    val loadingState: Flow<DataLoadingState>

    fun prepareReload()
}