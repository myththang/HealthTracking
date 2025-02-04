package com.fpt.edu.healthtracking.ui.streak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.StreakRepository
import com.fpt.edu.healthtracking.data.responses.ExerciseStreakResponse
import com.fpt.edu.healthtracking.data.responses.FoodStreakResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StreakViewModel(private val repository: StreakRepository) : ViewModel() {
    private val _streakData = MutableLiveData<FoodStreakResponse>()
    val streakData: LiveData<FoodStreakResponse> = _streakData

    private val _streakExerciseData = MutableLiveData<ExerciseStreakResponse>()
    val streakExerciseData: LiveData<ExerciseStreakResponse> = _streakExerciseData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _currentMonth = MutableLiveData(Calendar.getInstance())
    val currentMonth: LiveData<Calendar> = _currentMonth

    private val _showCongratulations = MutableLiveData<Int?>()
    val showCongratulations: MutableLiveData<Int?> = _showCongratulations

    private var lastShownStreak: Int? = null

    fun getStreak(date: Calendar = Calendar.getInstance()) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = formatter.format(Calendar.getInstance().time)

                when (val result = repository.getStreak(dateString)) {
                    is Resource.Success -> {
                        _streakData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load streak data"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getExerciseStreak(date: Calendar = Calendar.getInstance()) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = formatter.format(Calendar.getInstance().time)

                when (val result = repository.getExerciseStreak(dateString)) {
                    is Resource.Success -> {
                        _streakExerciseData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load streak data"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun navigateMonth(offset: Int) {
        _currentMonth.value = _currentMonth.value?.apply {
            add(Calendar.MONTH, offset)
        }
        lastShownStreak = null
    }

    fun congratulationsShown() {
        _showCongratulations.value = null
    }

    fun showCongratulations() {
        streakData.value?.let { data ->
            if (data.streakNumber > 0 && lastShownStreak != data.streakNumber) {
                _showCongratulations.value = data.streakNumber
                lastShownStreak = data.streakNumber
            }
        }
        streakExerciseData.value?.let { data ->
            if (data.streakCount > 0 && lastShownStreak != data.streakCount) {
                _showCongratulations.value = data.streakCount
                lastShownStreak = data.streakCount
            }
        }
    }


}
