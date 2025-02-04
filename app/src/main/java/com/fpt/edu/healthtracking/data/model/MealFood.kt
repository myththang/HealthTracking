package com.fpt.edu.healthtracking.data.model

data class MealFood(
    val food: Food,
    val quantity: Int = 1,
    val unit: String = "serving"
) {
    val totalCalories: Int = (food.calories * quantity).toInt()
    val totalProtein: Float = food.protein * quantity
    val totalCarbs: Float = food.carbs * quantity
    val totalFat: Float = food.fat * quantity
}