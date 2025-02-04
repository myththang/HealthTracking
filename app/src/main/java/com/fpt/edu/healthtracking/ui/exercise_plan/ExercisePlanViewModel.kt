package com.fpt.edu.healthtracking.ui.exercise_plan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.ExercisePlan
import com.fpt.edu.healthtracking.data.model.ExercisePlanDetail
import com.fpt.edu.healthtracking.data.repository.ExercisePlanRepository
import kotlinx.coroutines.launch

class ExercisePlanViewModel(
    private val repository: ExercisePlanRepository
) : ViewModel() {
    private val _plans = MutableLiveData<List<ExercisePlan>>()
    val plans: LiveData<List<ExercisePlan>> = _plans

    private val _planDetail = MutableLiveData<ExercisePlanDetail>()
    val planDetail: LiveData<ExercisePlanDetail> = _planDetail

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _assignSuccess = MutableLiveData<Boolean>()
    val assignSuccess: LiveData<Boolean> = _assignSuccess

    fun loadPlans() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getAllPlans()) {
                    is Resource.Success -> _plans.value = result.value
                    is Resource.Failure -> _error.value = "Không thể tải danh sách kế hoạch"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPlanDetail(planId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getPlanDetail(planId)) {
                    is Resource.Success -> _planDetail.value = result.value
                    is Resource.Failure -> _error.value = "Không thể tải chi tiết kế hoạch"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchPlans(query: String) {
        viewModelScope.launch {
            try {
                when (val result = repository.searchPlans(query)) {
                    is Resource.Success -> _plans.value = result.value
                    is Resource.Failure -> _error.value = "Không thể tìm kiếm kế hoạch"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun assignPlan(planId: Int, startDate: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.assignPlan(planId, startDate)) {
                    is Resource.Success -> _assignSuccess.value = true
                    is Resource.Failure -> _error.value = "Không thể thêm kế hoạch"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}