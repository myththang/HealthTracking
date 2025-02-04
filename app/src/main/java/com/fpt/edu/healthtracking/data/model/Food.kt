package com.fpt.edu.healthtracking.data.model

data class Food(
    val foodId: Int,
    val foodName: String,
    val foodImage: String,
    val calories: Double,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val dietName: String
)