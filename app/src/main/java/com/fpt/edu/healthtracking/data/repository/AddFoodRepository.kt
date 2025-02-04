package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.Food
import kotlinx.coroutines.flow.first

class AddFoodRepository(
    private val api: FoodApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    suspend fun getAllFoods(): Resource<List<Food>> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getAllFoods(token)
        }
    }

    suspend fun searchFoods(query: String): Resource<List<Food>> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.searchFoods(query, token)
        }
    }

    suspend fun getFoodHistory() = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getFoodHistory(token)
    }

    suspend fun getFoodSuggestions() = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getFoodSuggestions(token)
    }
}