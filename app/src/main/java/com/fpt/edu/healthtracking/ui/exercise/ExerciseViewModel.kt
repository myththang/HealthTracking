package com.fpt.edu.healthtracking.ui.exercise

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.ExerciseCategory
import com.fpt.edu.healthtracking.data.repository.ExerciseRepository
import com.fpt.edu.healthtracking.data.responses.CardioResponse
import com.fpt.edu.healthtracking.data.responses.ExerciseDiaryDetail
import com.fpt.edu.healthtracking.data.responses.ExerciseDiaryResponse
import com.fpt.edu.healthtracking.data.responses.ExerciseListResponseItem
import com.fpt.edu.healthtracking.data.responses.OtherResponse
import com.fpt.edu.healthtracking.data.responses.ResistanceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ExerciseViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _exerciseDiary = MutableLiveData<ExerciseDiaryResponse>()
    val exerciseDiary: LiveData<ExerciseDiaryResponse> = _exerciseDiary

    private val _exercises = MutableLiveData<List<ExerciseListResponseItem>>()
    val exercises: LiveData<List<ExerciseListResponseItem>> = _exercises

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentCategory = ExerciseCategory.ALL

    private val _exerciseDetail = MutableLiveData<ExerciseDetailInfo>()
    val exerciseDetail: LiveData<ExerciseDetailInfo> = _exerciseDetail

    private val _workoutLevels = MutableLiveData<List<WorkoutLevel>>()
    val workoutLevels: LiveData<List<WorkoutLevel>> = _workoutLevels

    private val _addToDiaryResult = MutableLiveData<Boolean>()
    val addToDiaryResult: LiveData<Boolean> = _addToDiaryResult

    private val _updateToDiaryResult = MutableLiveData<Boolean>()
    val updateToDiaryResult: LiveData<Boolean> = _updateToDiaryResult


    fun loadExerciseDetail(exerciseId: Int, isCardio: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (isCardio==1) {
                    when (val result = repository.getCardioDetail(exerciseId)) {
                        is Resource.Success<CardioResponse> -> {
                            handleCardioResponse(result.value)
                        }
                        is Resource.Failure -> handleError(result)
                    }
                }
                if(isCardio==2) {
                    when (val result = repository.getResistanceDetail(exerciseId)) {
                        is Resource.Success<ResistanceResponse> -> {
                            handleResistanceResponse(result.value)
                        }
                        is Resource.Failure -> handleError(result)
                    }
                }
                if(isCardio==3) {
                    when (val result = repository.getOtherDetail(exerciseId)) {
                        is Resource.Success<OtherResponse> -> {
                            handleOtherResponse(result.value)
                        }
                        is Resource.Failure -> handleError(result)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun handleCardioResponse(response: CardioResponse) {
        _exerciseDetail.value = ExerciseDetailInfo(
            exerciseId = response.exerciseId,
            exerciseName = response.exerciseName,
            exerciseImage = response.exerciseImage,
            description = response.description,
            categoryExercise = response.categoryExercise,
            met = response.metValue
        )

        _workoutLevels.value = listOf(
            WorkoutLevel("Nhẹ", minutes = response.minutes1, calories = response.calories1),
            WorkoutLevel("Trung bình", minutes = response.minutes2, calories = response.calories2),
            WorkoutLevel("Nặng", minutes = response.minutes3, calories = response.calories3)
        )
    }

    private fun handleResistanceResponse(response: ResistanceResponse) {
        _exerciseDetail.value = ExerciseDetailInfo(
            exerciseId = response.exerciseId,
            exerciseName = response.exerciseName,
            exerciseImage = response.exerciseImage,
            description = response.description,
            categoryExercise = response.categoryExercise,
            met = response.metValue
        )

        _workoutLevels.value = listOf(
            WorkoutLevel("Nhẹ", reps = response.reps1, sets = response.sets1, minutes = response.minutes1),
            WorkoutLevel("Trung bình", reps = response.reps2, sets = response.sets2, minutes = response.minutes2),
            WorkoutLevel("Nặng", reps = response.reps3, sets = response.sets3, minutes = response.minutes3)
        )
    }
    private fun handleOtherResponse(response: OtherResponse) {
        _exerciseDetail.value = ExerciseDetailInfo(
            exerciseId = response.exerciseId,
            exerciseName = response.exerciseName,
            exerciseImage = response.exerciseImage,
            description = response.description,
            categoryExercise = response.categoryExercise,
            met = response.metValue
        )
    }


    fun loadExerciseDiary(date: Date) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getExerciseDiaryByDate(date)) {
                    is Resource.Success -> {
                        Log.d("ExerciseViewModel", "Success: ${result.value}")
                        _exerciseDiary.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        Log.e("ExerciseViewModel", "Failure: code=${result.errorCode}, isNetwork=${result.isNetworkError}")
                        _exerciseDiary.value = ExerciseDiaryResponse(
                            exerciseDiaryId = 0,
                            memberId = 0,
                            exercisePlanId = null,
                            date = "",
                            totalDuration = 0f,
                            totalCaloriesBurned = 0f,
                            exerciseDiaryDetails = emptyList()
                        )
                        _error.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Exception: ${e.message}", e)
                _error.value = null
                _exerciseDiary.value = ExerciseDiaryResponse(
                    exerciseDiaryId = 0,
                    memberId = 0,
                    exercisePlanId = null,
                    date = "",
                    totalDuration = 0f,
                    totalCaloriesBurned = 0f,
                    exerciseDiaryDetails = emptyList()
                )
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadExercises() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getExerciseList("", getCurrentCategoryFilter())) {
                    is Resource.Success -> {
                        _exercises.value = result.value
                    }
                    is Resource.Failure -> {
                        handleError(result)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchExercises(query: String) {
        viewModelScope.launch {
            try {
                when (val result = repository.getExerciseList(query, getCurrentCategoryFilter())) {
                    is Resource.Success -> {
                        _exercises.value = result.value
                    }
                    is Resource.Failure -> {
                        handleError(result)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addToDiary(diaryId:Int,exerciseId: Int,duration:Int,isPractice:Boolean,caloriesBurned:Int ){
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.addToDiary(diaryId, exerciseId, duration, isPractice,caloriesBurned)) {
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

    fun setCategory(category: ExerciseCategory) {
        currentCategory = category
        loadExercises()
    }

    private fun getCurrentCategoryFilter(): Int? {
        return when (currentCategory) {
            ExerciseCategory.ALL -> null
            ExerciseCategory.CARDIO -> 1
            ExerciseCategory.STRENGTH -> 2
            ExerciseCategory.OTHER -> 3
        }
    }

    private fun handleError(failure: Resource.Failure) {
        _error.value = when {
            failure.isNetworkError -> "Please check your internet connection"
            failure.errorCode == 401 -> "Please log in again"
            failure.errorCode == 404 -> "Exercises not found"
            else -> "An error occurred"
        }
    }

    fun updateStatus(workout: ExerciseDiaryDetail) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.updateState(workout.exerciseDiaryId, workout.exerciseDiaryDetailsId, workout.isPractice)) {
                    is Resource.Success -> _updateToDiaryResult.value = true
                    is Resource.Failure -> _error.value = "Failed to update practice"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

}

data class ExerciseDetailInfo(
    val exerciseId: Int,
    val exerciseName: String,
    val exerciseImage: String,
    val description: String,
    val categoryExercise: String,
    val met: Double
)

data class WorkoutLevel(
    val level: String,
    val reps: Int? = null,
    val sets: Int? = null,
    val minutes: Int? = null,
    val calories: Int? = null
)


