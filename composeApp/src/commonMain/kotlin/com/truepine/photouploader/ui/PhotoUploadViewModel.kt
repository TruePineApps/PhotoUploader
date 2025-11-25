package com.truepine.photouploader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.io.KmpFile
import com.truepine.photouploader.auth.GoogleAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoUploadViewModel(
    private val authService: GoogleAuthService
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        // Automatically check for existing sign-in when the ViewModel is created
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            val token = authService.restoreSignIn()
            if (token != null) {
                _isAuthenticated.value = true
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            val token = authService.signIn()
            if (token != null) {
                _isAuthenticated.value = true
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _isAuthenticated.value = false
        }
    }

    fun uploadPhotos(folder: KmpFile) {

    }
}
