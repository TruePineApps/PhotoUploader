package com.truepineapps.photouploader.core.domain.state

sealed interface DataLoadingState {
    data object Success : DataLoadingState
    data class Error(val exception: Throwable) : DataLoadingState
    data object Loading : DataLoadingState
}