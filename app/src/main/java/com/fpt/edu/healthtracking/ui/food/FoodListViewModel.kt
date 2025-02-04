package com.fpt.edu.healthtracking.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.repository.FoodRepository
import kotlinx.coroutines.launch

class FoodListViewModel(
    private val repository: FoodRepository
) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Food>>()
    val searchResults: LiveData<List<Food>> = _searchResults

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun searchFoods(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.searchFoods(query)) {
                    is Resource.Success -> {
                        _searchResults.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = when {
                            result.isNetworkError -> "Network error"
                            else -> "Error searching foods"
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }
}