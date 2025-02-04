package com.fpt.edu.healthtracking.ui.food

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.MealFood
import com.fpt.edu.healthtracking.data.repository.CreateMealRepository
import com.fpt.edu.healthtracking.data.repository.PreviousMealRepository
import com.fpt.edu.healthtracking.data.request.CreateMealRequest
import com.fpt.edu.healthtracking.data.request.MealDetailRequest
import com.fpt.edu.healthtracking.data.responses.FoodDetail
import com.fpt.edu.healthtracking.data.responses.MealDetailResponse
import com.fpt.edu.healthtracking.data.responses.PreviousMealResponse
import com.fpt.edu.healthtracking.ui.food.MealDetailViewModel.NutritionInfo
import com.fpt.edu.healthtracking.ui.food.MealDetailViewModel.SavingState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class PreviousFoodViewModel(
    private val repository: PreviousMealRepository
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

    fun getPreviousMeal(diaryId: Int, mealType: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getPreviousMeal(diaryId,mealType)) {
                    is Resource.Success -> {
                        val response = result.value
                        _mealName.value = let {
                            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val date = formatter.parse(response.getDatePrevious)

                            val calendar = Calendar.getInstance()
                            val yesterday = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -1)
                            }

                            date?.let {
                                val dateCalendar = Calendar.getInstance().apply {
                                    time = it
                                }

                                when {
                                    dateCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                                            dateCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
                                        "Bữa ăn hôm qua"
                                    }
                                    else -> {
                                        val outputFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
                                        "Bữa ăn ngày ${outputFormatter.format(date)}"
                                    }
                                }
                            } ?: "Bữa ăn"
                        }
                        _mealImage.value = ""
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

    private fun calculateMacroPercentage(macro: Double, response: PreviousMealResponse): Int {
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

    fun copyPreviousMeal(diaryId: Int, mealType: Int, currentDiaryId: Int) {
        viewModelScope.launch {
            _savingState.value = SavingState.Saving
            try {
                when (val result = repository.copyPreviousMeal(diaryId, mealType, currentDiaryId)) {
                    is Resource.Success -> {
                        _savingState.value = SavingState.Success
                    }
                    is Resource.Failure -> {
                        _savingState.value = SavingState.Error("Không thể sao chép bữa ăn")
                    }
                }
            } catch (e: Exception) {
                _savingState.value = SavingState.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
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