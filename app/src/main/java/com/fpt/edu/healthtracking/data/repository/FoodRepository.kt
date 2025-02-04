package com.fpt.edu.healthtracking.data.repository

import android.util.Log
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.MemberMeal
import com.fpt.edu.healthtracking.data.request.AddFoodToDiaryRequest
import com.fpt.edu.healthtracking.data.request.AddMealRequest
import com.fpt.edu.healthtracking.data.responses.MealDetailResponse
import kotlinx.coroutines.flow.first

class FoodRepository(
    private val api: FoodApi,
    private val userPreferences: UserPreferences
) : BaseRepository() {

    suspend fun searchFoods(query: String): Resource<List<Food>> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            if (!authState.isLoggedIn) {
                throw Exception("Unauthorized")
            }
            val token = "Bearer ${authState.accessToken}"
            api.searchFoods(query, token)
        }
    }

    suspend fun getMemberMeals(): Resource<List<MemberMeal>> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            Log.d("FoodRepo", "Getting meals with token: $token")
            val meals = api.getAllMealMembers(token)
            Log.d("FoodRepo", "Got meals: ${meals.size}")
            meals
        }
    }
    suspend fun getMemberMealById(mealId: Int): Resource<MealDetailResponse> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getMealMembers(token,mealId)
        }
    }

    suspend fun checkPreviousMeals(mealType: Int) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getMealBeforeByMealType(token, mealType)
    }

    suspend fun addFoodToDiary(foodId: Int, diaryId: Int, mealType: Int, quantity: Int) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"

        api.addFoodToDiary(
            token = token,
            request = AddFoodToDiaryRequest(
                diaryId = diaryId,
                foodId = foodId,
                quantity = quantity,
                mealType = mealType
            )
        )
    }

    suspend fun getFoodDetail(foodId: Int, date: String) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getFoodDetail(foodId, date, token)
    }

    suspend fun addMealToDiary(diaryId: Int, mealId: Int, mealType: Int): Resource<String> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"

            val request = AddMealRequest(
                diaryId = diaryId,
                mealMemberId = mealId,
                mealType = mealType
            )

            api.addMealToDiary(token, request)
        }
    }

    suspend fun deleteMealMember(mealId: Int) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.deleteMealMember(token, mealId)
    }

}