package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.FoodDetail
import com.fpt.edu.healthtracking.data.model.MealType

class FoodDetailRepository : BaseRepository() {

    suspend fun getFoodDetail(foodId: Int): Resource<FoodDetail> {
        return safeApiCall {
            // This is where you'd normally make an API call
            // For now, returning mock data
            FoodDetail(
                id = foodId,
                name = "Chicken Breast - Cooked",
                imageUrl = "https://example.com/chicken-breast.jpg",
                baseProtein = 31f,
                baseCarbs = 0f,
                baseFats = 3.6f,
                baseCalories = 165f,
                servingSize = 100f,
                cupSize = 240f
            )
        }
    }

    suspend fun addFoodToMeal(
        mealType: MealType,
        foodDetail: FoodDetail
    ): Resource<Boolean> {
        return safeApiCall {
            // Make API call to add food to meal
            // For now, just returning success
            true
        }
    }
}
