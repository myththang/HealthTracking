package com.fpt.edu.healthtracking.ui.nutrition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.DailyCaloriesDto
import com.fpt.edu.healthtracking.data.model.DailyNutritionDto
import com.fpt.edu.healthtracking.data.model.MacroNutrientDto
import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.data.repository.NutritionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class NutritionViewModel(
    private var repository:NutritionRepository

): ViewModel() {

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _caloriesData = MutableLiveData<DailyCaloriesDto?>()
    val caloriesData: LiveData<DailyCaloriesDto?> = _caloriesData

    private val _nutrientData = MutableLiveData<DailyNutritionDto?>()
    val nutrientData: LiveData<DailyNutritionDto?> = _nutrientData

    private val _macroData = MutableLiveData<MacroNutrientDto?>()
    val macroData: LiveData<MacroNutrientDto?> = _macroData

    private val _datePicked = MutableLiveData<String>()
    val datePicked: LiveData<String> get() = _datePicked


    init {
        // Initialize the datePicked with today's date
        _datePicked.value = LocalDate.now().toString()
    }

    fun setDatePicked(date: String) {
        _datePicked.value = date
    }

    // Function to fetch daily calories
    fun fetchDailyCalories(date: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getDailyCalories(date)) {
                    is Resource.Success -> {
                        _caloriesData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load daily calories: ${result.errorBody?.string()}"
                        _caloriesData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _caloriesData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    // Function to fetch daily nutrition
    fun fetchDailyNutrition(date: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getDailyNutrition(date)) {
                    is Resource.Success -> {
                        _nutrientData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load daily nutrition: ${result.errorBody?.string()}"
                        _nutrientData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _nutrientData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    // Function to fetch daily macros
    fun fetchDailyMacros(date: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getDailyMacros(date)) {
                    is Resource.Success -> {
                        _macroData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load daily macros: ${result.errorBody?.string()}"
                        _macroData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _macroData.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}