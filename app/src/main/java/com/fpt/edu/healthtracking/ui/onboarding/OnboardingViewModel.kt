package com.fpt.edu.healthtracking.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.OnboardingPage
import com.fpt.edu.healthtracking.data.repository.OnboardingRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: OnboardingRepository) : ViewModel() {

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> = _currentPage

    private val _onboardingCompleted = MutableLiveData<Boolean>()
    val onboardingCompleted: LiveData<Boolean> = _onboardingCompleted

    private val _onboardingPages = MutableLiveData<List<OnboardingPage>>()
    val onboardingPages: LiveData<List<OnboardingPage>> = _onboardingPages

    init {
        loadOnboardingPages()
    }

    private fun loadOnboardingPages() {
        viewModelScope.launch {
            _onboardingPages.value = repository.getOnboardingPages()
        }
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            _onboardingCompleted.value = true
        }
    }
}