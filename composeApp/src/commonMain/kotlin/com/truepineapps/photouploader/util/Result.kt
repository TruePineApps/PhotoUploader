package com.truepineapps.photouploader.util

sealed interface Result<out D, out E> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E>(val error: E): Result<Nothing, E>
    data class Exception(val throwable: Throwable): Result<Nothing, Nothing>
}
