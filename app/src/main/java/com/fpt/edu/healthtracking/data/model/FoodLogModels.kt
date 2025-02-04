package com.fpt.edu.healthtracking.data.model


data class FoodLogState(
    val targetCalories: Int = 1750,
    val consumedCalories: Int = 1500,
    val exerciseCalories: Int = 500,
    val remainingCalories: Int = 0,
    val diaryId: Int = -1,
    val meals: List<Meal> = emptyList()
)

data class Meal(
    val type: MealType,
    val foods: List<FoodLog>,
    val targetCalories: Int,
    val consumedCalories: Int = foods.sumOf { it.calories }
)

data class FoodLog(
    val diaryDetailId: Int,
    val foodId: Int,
    val foodName: String,
    val calories: Int,
    val servingSize: String,
    val mealType: Int
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}