package com.fpt.edu.healthtracking.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.FoodDetail
import com.fpt.edu.healthtracking.data.repository.FoodDetailRepository
import com.fpt.edu.healthtracking.data.repository.FoodRepository
import com.fpt.edu.healthtracking.data.responses.FoodDetailResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

class FoodDetailViewModel(private val repository: FoodRepository) : ViewModel() {
    private val _foodDetail = MutableLiveData<FoodDetailResponse>()
    val foodDetail: LiveData<FoodDetailResponse> = _foodDetail

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _addToDiaryResult = MutableLiveData<Boolean>()
    val addToDiaryResult: LiveData<Boolean> = _addToDiaryResult

    fun loadFoodDetail(foodId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val today = dateFormat.format(Date())

                when (val result = repository.getFoodDetail(foodId, today)) {
                    is Resource.Success -> _foodDetail.value = result.value
                    is Resource.Failure -> _error.value = "Failed to load food details"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun addFoodToDiary(foodId: Int, diaryId: Int, mealType: Int, quantity: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.addFoodToDiary(foodId, diaryId, mealType, quantity)) {
                    is Resource.Success -> _addToDiaryResult.value = true
                    is Resource.Failure -> _error.value = "Failed to add food to diary"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun calculatePercentage(value: Float, total: Float): Int {
        return ((value / total) * 100).toInt().coerceIn(0, 100)
    }
}

