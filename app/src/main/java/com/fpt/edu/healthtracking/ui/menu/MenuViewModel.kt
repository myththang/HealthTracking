package com.fpt.edu.healthtracking.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.Menu
import com.fpt.edu.healthtracking.data.model.MenuDetail
import com.fpt.edu.healthtracking.data.repository.MenuRepository
import kotlinx.coroutines.launch

class MenuViewModel(
    private val repository: MenuRepository
) : ViewModel() {
    private val _menus = MutableLiveData<List<Menu>>()
    val menus: LiveData<List<Menu>> = _menus

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _menuDetail = MutableLiveData<MenuDetail>()
    val menuDetail: LiveData<MenuDetail> = _menuDetail

    private val _actionSuccess = MutableLiveData<Boolean>()
    val actionSuccess: LiveData<Boolean> = _actionSuccess

    private var currentMealPlanId: Int = -1

    private val _selectedDay = MutableLiveData<Int>(1)
    val selectedDay: LiveData<Int> = _selectedDay

    fun setDay(day: Int) {
        _selectedDay.value = day
        currentMealPlanId.takeIf { it != -1 }?.let {
            loadMenuDetail(it, day)
        }
    }

    fun loadMenuDetail(mealPlanId: Int, day: Int = selectedDay.value ?: 1) {
        currentMealPlanId = mealPlanId
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getMenuDetail(mealPlanId, day)) {
                    is Resource.Success -> {
                        _menuDetail.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load menu detail"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    fun loadMenus() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getMenus()) {
                    is Resource.Success -> {
                        _menus.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load menus"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchMenus(query: String) {
        viewModelScope.launch {
            try {
                when (val result = repository.searchMenus(query)) {
                    is Resource.Success -> {
                        _menus.value = result.value
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to search menus"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    private fun handleError(failure: Resource.Failure) {
        _error.value = when {
            failure.isNetworkError -> "Please check your internet connection"
            failure.errorCode == 401 -> "Please log in again"
            failure.errorCode == 404 -> "Menu not found"
            else -> "An error occurred"
        }
    }

    fun addFullMenuPlan(startDate: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.addMenuPlanToWeek(currentMealPlanId, startDate)) {
                    is Resource.Success -> _actionSuccess.value = true
                    is Resource.Failure -> handleError(result)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun addDayToDate(selectedDay: Int, selectedDate: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.addMenuPlanDay(currentMealPlanId, selectedDay, selectedDate)) {
                    is Resource.Success -> _actionSuccess.value = true
                    is Resource.Failure -> handleError(result)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun addMealToDate(
        selectedDay: Int,
        mealTypeDay: Int,
        selectedMealType: Int,
        selectedDate: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.addMenuPlanMeal(
                    currentMealPlanId,
                    selectedDay,
                    mealTypeDay,
                    selectedMealType,
                    selectedDate
                )) {
                    is Resource.Success -> _actionSuccess.value = true
                    is Resource.Failure -> handleError(result)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}