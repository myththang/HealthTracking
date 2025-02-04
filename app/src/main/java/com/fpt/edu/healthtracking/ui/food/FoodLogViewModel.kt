package com.fpt.edu.healthtracking.ui.food

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.FoodLogState
import com.fpt.edu.healthtracking.data.repository.FoodLogRepository
import com.fpt.edu.healthtracking.data.responses.FoodStreakResponse
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.launch
import java.util.Date

class FoodLogViewModel(
    private val repository: FoodLogRepository
) : ViewModel() {
    private val _foodLogState = MutableLiveData<FoodLogState>()
    val foodLogState: LiveData<FoodLogState> = _foodLogState

    private val _specialDays = MutableLiveData<List<CalendarDay>>()
    val specialDays: LiveData<List<CalendarDay>> = _specialDays

    private val _delete = MutableLiveData<Boolean>()
    val delete: LiveData<Boolean> = _delete

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadFoodLog(token: String, date: Date) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getFoodLog(token, date)) {
                    is Resource.Success -> {
                        Log.d("FoodLogViewModel", "Response data: ${result.value}")
                        
                        if (result.value.diaryId != null) {
                            _foodLogState.value = result.value
                        } else {
                            _foodLogState.value = FoodLogState(
                                diaryId = 0,
                                targetCalories = result.value.targetCalories ?: 0,
                                consumedCalories = 0,
                                remainingCalories = result.value.targetCalories ?: 0,
                                meals = emptyList()
                            )
                        }
                    }
                    is Resource.Failure -> {
                        Log.e("FoodLogViewModel", "Failed to load food log: ${result}")
                        _error.value = "Không thể tải dữ liệu nhật ký ăn uống"
                    }
                }
            } catch (e: Exception) {
                Log.e("FoodLogViewModel", "Exception when loading food log", e)
                _error.value = "Đã xảy ra lỗi: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    fun deleteFoodFromDiary(diaryDetailId: Int) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteFoodFromDiary(diaryDetailId)) {
                    is Resource.Success -> {
                        _delete.value = true
                        _error.value = "xóa món ăn thành công"
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể xóa món ăn. Vui lòng thử lại sau"
                        _delete.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = "Đã xảy ra lỗi: ${e.message}"
                _delete.value = false
            }
        }
    }
    fun getSpecialDays(token: String, onComplete: (List<CalendarDay>) -> Unit) {
        viewModelScope.launch {
            try {
                when (val result = repository.getSpecialDays(token)) {
                    is Resource.Success -> {
                        _specialDays.value = result.value
                        onComplete(result.value)
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể tải danh sách ngày đặc biệt"
                        onComplete(emptyList())
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Đã xảy ra lỗi"
                onComplete(emptyList())
            }
        }
    }
}