package com.fpt.edu.healthtracking.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.GraphData
import com.fpt.edu.healthtracking.data.repository.GoalRepository
import kotlinx.coroutines.launch

class ProgressViewModel (
    private var repository: GoalRepository
):ViewModel() {

    private val _goalData = MutableLiveData<GraphData?>()
    val goalData: LiveData<GraphData?> = _goalData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getWeightData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getWeightGraphDetail()) {
                    is Resource.Success -> {
                        _goalData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to get graph: ${result.errorBody?.string()}"
                        _goalData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

}