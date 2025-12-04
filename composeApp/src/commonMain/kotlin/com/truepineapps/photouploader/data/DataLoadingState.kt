package com.truepineapps.photouploader.data

sealed interface DataLoadingState {
    data object Success : DataLoadingState
    data class Error(val exception: Throwable) : DataLoadingState
    data object Loading : DataLoadingState
}