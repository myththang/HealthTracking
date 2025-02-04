package com.fpt.edu.healthtracking.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.MealDetailRepository
import com.fpt.edu.healthtracking.data.responses.FoodDetail
import com.fpt.edu.healthtracking.data.responses.MealDetailResponse
import kotlinx.coroutines.launch

import kotlin.math.roundToInt

class MealDetailViewModel(
    private val repository: MealDetailRepository
) : ViewModel() {
    private val _mealName = MutableLiveData<String>()
    val mealName: LiveData<String> = _mealName

    private val _mealImage = MutableLiveData<String>()
    val mealImage: LiveData<String> = _mealImage

    private val _nutritionInfo = MutableLiveData(NutritionInfo())
    val nutritionInfo: LiveData<NutritionInfo> = _nutritionInfo

    private val _mealFoods = MutableLiveData<List<FoodDetail>>()
    val mealFoods: LiveData<List<FoodDetail>> = _mealFoods

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _savingState = MutableLiveData<SavingState>()
    val savingState: LiveData<SavingState> = _savingState

    fun addMealToDiary(diaryId: Int, mealId: Int, mealType: Int) {
        viewModelScope.launch {
            _savingState.value = SavingState.Saving
            try {
                when (val result = repository.addMealToDiary(diaryId, mealId, mealType)) {
                    is Resource.Success -> {
                        _savingState.value = SavingState.Success
                    }

                    is Resource.Failure -> {
                        val errorMessage = when {
                            result.isNetworkError -> "Network error"
                            result.errorCode != null -> "Server error: ${result.errorCode}"
                            else -> "Unknown error occurred"
                        }
                        _savingState.value = SavingState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                _savingState.value = SavingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    fun loadMealDetail(mealId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getMealDetail(mealId)) {
                    is Resource.Success -> {
                        val response = result.value
                        _mealName.value = response.nameMealPlanMember
                        _mealImage.value = response.imageMealMember
                        _mealFoods.value = response.foodDetails

                        _nutritionInfo.value = NutritionInfo(
                            calories = response.totalCalories,
                            carbs = response.totalCarb.toFloat(),
                            fat = response.totalFat.toFloat(),
                            protein = response.totalProtein.toFloat(),
                            carbsPercent = calculateMacroPercentage(response.totalCarb, response),
                            fatPercent = calculateMacroPercentage(response.totalFat, response),
                            proteinPercent = calculateMacroPercentage(response.totalProtein, response)
                        )
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load meal details"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    private fun calculateMacroPercentage(macro: Double, response: MealDetailResponse): Int {
        val totalCals = (response.totalCarb * 4) + (response.totalFat * 9) + (response.totalProtein * 4)
        return if (totalCals > 0) {
            when (macro) {
                response.totalCarb -> ((response.totalCarb * 4 / totalCals) * 100).roundToInt()
                response.totalFat -> ((response.totalFat * 9 / totalCals) * 100).roundToInt()
                response.totalProtein -> ((response.totalProtein * 4 / totalCals) * 100).roundToInt()
                else -> 0
            }
        } else 0
    }


    data class NutritionInfo(
        val calories: Int = 0,
        val carbs: Float = 0f,
        val fat: Float = 0f,
        val protein: Float = 0f,
        val carbsPercent: Int = 0,
        val fatPercent: Int = 0,
        val proteinPercent: Int = 0
    )

    sealed class SavingState {
        object Idle : SavingState()
        object Saving : SavingState()
        object Success : SavingState()
        data class Error(val message: String) : SavingState()
    }
}