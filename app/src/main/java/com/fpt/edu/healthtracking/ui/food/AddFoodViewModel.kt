package com.fpt.edu.healthtracking.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.repository.AddFoodRepository
import kotlinx.coroutines.launch

class AddFoodViewModel(
    private val repository: AddFoodRepository
) : ViewModel() {
    private val _foods = MutableLiveData<List<Food>>()
    val foods: LiveData<List<Food>> = _foods

    private val _historyFoods = MutableLiveData<List<Food>>()
    val historyFoods: LiveData<List<Food>> = _historyFoods

    private val _suggestedFoods = MutableLiveData<List<Food>>()
    val suggestedFoods: LiveData<List<Food>> = _suggestedFoods

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun searchFood(query: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                when (val result = repository.searchFoods(query)) {
                    is Resource.Success -> {
                        _foods.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to search foods"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadHistoryFoods() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getFoodHistory()) {
                    is Resource.Success -> {
                        _historyFoods.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể tải lịch sử món ăn"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSuggestedFoods() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getFoodSuggestions()) {
                    is Resource.Success -> {
                        _suggestedFoods.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể tải gợi ý món ăn"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

}
