package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.DashboardApi
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.data.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class StreakRepository(
    private val api: DashboardApi,
    private val preferences: UserPreferences
) : BaseRepository() {
    suspend fun getStreak(date: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getFoodStreak(token, date)
    }

    suspend fun getExerciseStreak(date: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getExerciseStreak(token, date)
    }
}