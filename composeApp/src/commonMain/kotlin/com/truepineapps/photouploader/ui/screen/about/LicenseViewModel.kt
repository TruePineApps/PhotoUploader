package com.truepineapps.photouploader.ui.screen.about


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truepineapps.photouploader.resources.Res
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

class LicenseViewModel : ViewModel() {
    private val _fontResult = MutableStateFlow<LoadLicenseResult>(LoadLicenseResult.Loading)
    val fontResult = _fontResult.asStateFlow()

    private val _noticesResult = MutableStateFlow<LoadLicenseResult>(LoadLicenseResult.Loading)
    val noticesResult = _noticesResult.asStateFlow()

    init {
        loadLicenses()
    }

    private fun loadLicenses() {
        viewModelScope.launch {
            // Read files concurrently
            val fontJob = async { loadLicenseFile("OFL.txt") }
            val noticesJob = async { loadLicenseFile("NOTICES") }

            _fontResult.value = fontJob.await()
            _noticesResult.value = noticesJob.await()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadLicenseFile(fileName: String): LoadLicenseResult = try {
        val bytes = Res.readBytes("files/$fileName")
        LoadLicenseResult.Success(bytes.decodeToString())
    } catch (e: Exception) {
        LoadLicenseResult.Error(e)
    }
}

sealed interface LoadLicenseResult {
    data object Loading : LoadLicenseResult
    data class Success(val licenseText: String) : LoadLicenseResult
    data class Error(val exception: Throwable) : LoadLicenseResult
}