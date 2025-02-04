package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.MenuApi
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.request.AddMenuPlanDayRequest
import com.fpt.edu.healthtracking.data.request.AddMenuPlanMealRequest
import com.fpt.edu.healthtracking.data.request.AddMenuToMealRequest
import kotlinx.coroutines.flow.first

class MenuRepository(
    private val api: MenuApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    suspend fun getMenus() = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getMenus(token)
    }

    suspend fun searchMenus(query: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.searchMenus(query, token)
    }
    suspend fun getMenuDetail(mealPlanId: Int, day: Int = 1) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getMenuDetail(mealPlanId, day, token)
    }

    suspend fun addMenuPlanToWeek(mealPlanId: Int, startDate: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.addMenuPlanToWeek(mealPlanId, startDate, token)
    }

    suspend fun addMenuPlanDay(mealPlanId: Int, selectedDay: Int, selectedDate: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        val request = AddMenuPlanDayRequest(
            selectDate = selectedDate,
            day = selectedDay,
            mealPlanId = mealPlanId
        )
        api.addMenuPlanDay(request, token)
    }

    suspend fun addMenuPlanMeal(
        mealPlanId: Int,
        selectedDay: Int,
        mealTypeDay: Int,
        selectedMealType: Int,
        selectedDate: String
    ) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        val request = AddMenuPlanMealRequest(
            mealTypeDay = mealTypeDay,
            selectMealTypeToAdd = selectedMealType,
            selectDateToAdd = selectedDate,
            day = selectedDay,
            mealPlanId = mealPlanId
        )
        api.addMenuPlanMeal(request, token)
    }
}