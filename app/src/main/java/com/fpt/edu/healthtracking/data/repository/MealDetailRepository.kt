package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.request.AddMealRequest
import com.fpt.edu.healthtracking.data.responses.MealDetailResponse
import kotlinx.coroutines.flow.first

class MealDetailRepository(
    private val api: FoodApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    suspend fun getMealDetail(mealId: Int): Resource<MealDetailResponse> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getMealMembers(token,mealId)
        }
    }

    suspend fun addMealToDiary(diaryId: Int, mealId: Int, mealType: Int): Resource<String> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            val request = AddMealRequest(
                diaryId = diaryId,
                mealMemberId = mealId,
                mealType = mealType
            )
            api.addMealToDiary(token, request)
        }
    }
}