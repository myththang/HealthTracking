package com.fpt.edu.healthtracking.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.repository.AddFoodRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddFoodToMealViewModel(
    private val repository: AddFoodRepository
) : ViewModel() {

    private val _foods = MutableLiveData<List<Food>>()
    val foods: LiveData<List<Food>> = _foods

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var searchJob: Job? = null

    init {
        loadAllFoods()
    }

    fun loadAllFoods() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getAllFoods()) {
                    is Resource.Success -> {
                        _foods.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load foods: ${result.errorBody?.string()}"
                        _foods.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _foods.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchFoods(query: String) {
        // Cancel previous search if any
        searchJob?.cancel()

        if (query.isBlank()) {
            loadAllFoods()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            _loading.value = true
            try {
                when (val result = repository.searchFoods(query)) {
                    is Resource.Success -> {
                        _foods.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Search failed: ${result.errorBody?.string()}"
                        _foods.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Search error: ${e.message}"
                _foods.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}