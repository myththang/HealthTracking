package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.NutritionApi
import com.fpt.edu.healthtracking.data.UserPreferences
import kotlinx.coroutines.flow.first

class NutritionRepository(
    private val api:NutritionApi,
    private val userPreferences: UserPreferences

):BaseRepository() {
    suspend fun getDailyCalories(date: String) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getDailyCalories(token, date)
    }

    suspend fun getDailyNutrition(date: String) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getDailyNutrition(token, date)
    }

    suspend fun getDailyMacros(date: String) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getDailyMacros(token, date)
    }
}