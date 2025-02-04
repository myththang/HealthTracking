package com.fpt.edu.healthtracking.ui.food

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.MealFood
import com.fpt.edu.healthtracking.data.repository.CreateMealRepository
import com.fpt.edu.healthtracking.data.request.CreateMealRequest
import com.fpt.edu.healthtracking.data.request.MealDetailRequest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CreateMealViewModel(
    private val repository: CreateMealRepository
) : ViewModel() {
    private val _mealName = MutableLiveData<String>()
    val mealName: LiveData<String> = _mealName

    private val _selectedFoods = MutableLiveData<List<MealFood>>(emptyList())
    val selectedFoods: LiveData<List<MealFood>> = _selectedFoods

    private val _canSave = MutableLiveData(false)
    val canSave: LiveData<Boolean> = _canSave

    private val _nutritionInfo = MutableLiveData(NutritionInfo())
    val nutritionInfo: LiveData<NutritionInfo> = _nutritionInfo

    private val _savingState = MutableLiveData<SavingState>(SavingState.Idle)
    val savingState: LiveData<SavingState> = _savingState

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setMealName(name: String) {
        _mealName.value = name
        updateSaveState()
    }

    fun addFood(food: Food, quantity: Int = 1) {
        val mealFood = MealFood(food = food, quantity = quantity)
        val currentList = _selectedFoods.value?.toMutableList() ?: mutableListOf()
        currentList.add(mealFood)
        _selectedFoods.value = currentList.toList()
        updateNutritionInfo(currentList)
        updateSaveState()
    }

    fun removeFood(mealFood: MealFood) {
        val currentList = _selectedFoods.value?.toMutableList() ?: mutableListOf()
        currentList.remove(mealFood)
        _selectedFoods.value = currentList
        updateNutritionInfo(currentList)
        updateSaveState()
    }

    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun hasSelectedImage(): Boolean {
        return _selectedImageUri.value != null
    }

    fun saveMeal(context: Context) {
        viewModelScope.launch {
            _savingState.value = SavingState.Saving

            try {
                val request = CreateMealRequest(
                    image = "default_meal_image.jpg",
                    nameMealMember = _mealName.value ?: "",
                    mealDetails = _selectedFoods.value?.map { mealFood ->
                        MealDetailRequest(
                            foodId = mealFood.food.foodId,
                            quantity = mealFood.quantity
                        )
                    } ?: emptyList()
                )

                when (val result = repository.createMeal(request)) {
                    is Resource.Success -> {
                        val mealId = result.value
                        // Upload image if selected
                        _selectedImageUri.value?.let { uri ->
                            when (val uploadResult = repository.uploadMealImage(mealId, uri, context)) {
                                is Resource.Success -> _savingState.value = SavingState.Success
                                is Resource.Failure -> _savingState.value = SavingState.Error(
                                    if (uploadResult.isNetworkError) "Lỗi kết nối mạng"
                                    else "Không thể tải lên ảnh"
                                )
                            }
                        } ?: run {
                            _savingState.value = SavingState.Success
                        }
                    }
                    is Resource.Failure -> {
                        _savingState.value = SavingState.Error(
                            if (result.isNetworkError) "Lỗi kết nối mạng"
                            else "Không thể lưu bữa ăn"
                        )
                    }
                }
            } catch (e: Exception) {
                _savingState.value = SavingState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    private fun updateSaveState() {
        val hasName = !_mealName.value.isNullOrBlank()
        val hasFoods = !_selectedFoods.value.isNullOrEmpty()
        _canSave.value = hasName && hasFoods
    }

    private fun updateNutritionInfo(foods: List<MealFood>) {
        val totalCals = foods.sumOf { it.totalCalories }
        val totalCarbs = foods.sumOf { it.totalCarbs.toDouble() }
        val totalFat = foods.sumOf { it.totalFat.toDouble() }
        val totalProtein = foods.sumOf { it.totalProtein.toDouble() }

        val carbsCals = totalCarbs * 4
        val fatCals = totalFat * 9
        val proteinCals = totalProtein * 4
        val totalMacroCals = carbsCals + fatCals + proteinCals

        val carbsPercent = if (totalMacroCals > 0) ((carbsCals / totalMacroCals) * 100).roundToInt() else 0
        val fatPercent = if (totalMacroCals > 0) ((fatCals / totalMacroCals) * 100).roundToInt() else 0
        val proteinPercent = if (totalMacroCals > 0) ((proteinCals / totalMacroCals) * 100).roundToInt() else 0

        _nutritionInfo.value = NutritionInfo(
            calories = totalCals,
            carbs = totalCarbs.toFloat(),
            fat = totalFat.toFloat(),
            protein = totalProtein.toFloat(),
            carbsPercent = carbsPercent,
            fatPercent = fatPercent,
            proteinPercent = proteinPercent
        )
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