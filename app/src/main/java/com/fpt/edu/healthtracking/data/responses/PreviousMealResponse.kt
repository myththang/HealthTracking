package com.fpt.edu.healthtracking.data.responses


data class PreviousMealResponse(
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarb: Double,
    val totalFat: Double,
    val getDatePrevious: String,
    val foodDetails: List<FoodDetail>
)