package com.fpt.edu.healthtracking.data.responses

data class FoodDetail(
    val calories: Int,
    val carbs: Double,
    val dietName: String,
    val fat: Double,
    val foodId: Int,
    val foodImage: String,
    val foodName: String,
    val protein: Double,
    val quantity: Int
)