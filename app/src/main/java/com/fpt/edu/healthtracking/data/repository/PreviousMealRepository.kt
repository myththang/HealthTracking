package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.responses.MealDetailResponse
import com.fpt.edu.healthtracking.data.responses.PreviousMealResponse
import kotlinx.coroutines.flow.first

class  PreviousMealRepository(
    private val api: FoodApi,
    private val preferences: UserPreferences
) : BaseRepository() {
    suspend fun getPreviousMeal(diaryId: Int, mealType: Int): Resource<PreviousMealResponse> {
        return safeApiCall {
            val token = "Bearer ${preferences.authStateFlow.first().accessToken}"
            api.getPreviousMeal(token, diaryId, mealType)
        }
    }

    suspend fun copyPreviousMeal(diaryIdPrevious: Int, mealTypePrevious: Int, diaryIdCurrent: Int): Resource<Unit> {
        return safeApiCall {
            val token = "Bearer ${preferences.authStateFlow.first().accessToken}"
            api.copyPreviousMeal(token, mapOf(
                "diaryIdPreviouseMeal" to diaryIdPrevious,
                "mealTypePreviousMeal" to mealTypePrevious,
                "diaryIdCurrent" to diaryIdCurrent
            ))
        }
    }
}