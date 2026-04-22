/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truepineapps.photouploader.data.DataLoadingRepository
import com.truepineapps.photouploader.data.DataLoadingState
import com.truepineapps.photouploader.util.UiText
import com.truepineapps.photouploader.util.UiTextString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
open class LoadingViewModel(
    private val dataLoadingRepository: DataLoadingRepository
) : ViewModel() {

    // A trigger to initiate/retry loading. Emitting to this will trigger a new load.
    // Using replay = 0 means it doesn't replay past values.
    // Using BufferOverflow.DROP_OLDEST to ensure only the latest reload signal is processed
    // if multiple reload calls happen quickly.
    private val reloadTrigger = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** The current state of loading the data */
    val loadingState: StateFlow<DataLoadingState> = reloadTrigger
        .onStart { emit(Unit) } // Emit an initial Unit to trigger the first load
        .flatMapLatest {
            // When reloadTrigger emits, flatMapLatest cancels the previous
            // collection of loadingState and starts a new one.
            dataLoadingRepository.loadingState.catch { emit(DataLoadingState.Error(it)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = DataLoadingState.Loading
        )

    /**
     * Display value of what is being loaded.
     * Base implementation returns the class name as a plain string. This does not work in a release.
     * Override with [com.truepineapps.photouploader.util.UiTextResource] to provide a translated name.
     */
    open fun getDisplayNameText(): UiText = UiTextString(this::class.simpleName ?: "")

    /**
     * Try reloading in case of an error
     * Try to emit to the trigger. If the buffer is full (e.g., if a load is already
     * in progress and another reload is called immediately), tryEmit might fail,
     * which is often fine as a load is already happening or queued.
     *
     * @return true if the reload was successful, false if not
     */
    fun reload(): Boolean {
        dataLoadingRepository.prepareReload()
        return reloadTrigger.tryEmit(Unit)
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}