package com.truepineapps.photouploader.core.feature.legal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truepineapps.photouploader.core.util.loadResourceFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LicenseViewModel : ViewModel() {
    private val _fontResult = MutableStateFlow<LoadLicenseResult>(LoadLicenseResult.Loading)
    val fontResult = _fontResult.asStateFlow()

    private val _noticesResult = MutableStateFlow<LoadLicenseResult>(LoadLicenseResult.Loading)
    val noticesResult = _noticesResult.asStateFlow()

    init {
        loadLicenses()
    }

    private fun loadLicenses() {
        viewModelScope.launch(Dispatchers.IO) {
            // Read files concurrently
            val fontJob = async { loadResourceFile("OFL.txt") }
            val noticesJob = async { loadResourceFile("NOTICES") }

            _fontResult.value = fontJob.await().toLoadLicenseResult()
            _noticesResult.value = noticesJob.await().toLoadLicenseResult()
        }
    }

}

sealed interface LoadLicenseResult {
    data object Loading : LoadLicenseResult
    data class Success(val licenseText: String) : LoadLicenseResult
    data class Error(val exception: Throwable) : LoadLicenseResult
}

private fun Result<String>.toLoadLicenseResult(): LoadLicenseResult =
    fold(
        onSuccess = { LoadLicenseResult.Success(licenseText = it) },
        onFailure = { LoadLicenseResult.Error(exception = it) }
    )