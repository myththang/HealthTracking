package com.fpt.edu.healthtracking.data.responses

data class FoodDetailResponse(
    val calories: Int,
    val carbs: Float,
    val dietId: Int,
    val dietName: String,
    val fat: Float,
    val foodId: Int,
    val foodImage: String,
    val foodName: String,
    val portion: String,
    val protein: Float,
    val status: Boolean,
    val totalCalories: Int,
    val totalCarb: Float,
    val totalFat: Float,
    val totalProtein: Float,
    val vitaminA: Float,
    val vitaminB1: Float,
    val vitaminB2: Float,
    val vitaminB3: Float,
    val vitaminC: Float,
    val vitaminD: Float,
    val vitaminE: Float
)