package com.fpt.edu.healthtracking.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WelcomeViewModel : ViewModel() {
    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> = _navigateToRegister

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    fun onGetStartedClicked() {
        _navigateToRegister.value = true
    }

    fun onLoginClicked() {
        _navigateToLogin.value = true
    }

    fun onNavigatedToRegister() {
        _navigateToRegister.value = false
    }

    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }
}