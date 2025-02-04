package com.fpt.edu.healthtracking.data.request

data class CreateMealRequest(
    val image: String,
    val nameMealMember: String,
    val mealDetails: List<MealDetailRequest>
)

data class MealDetailRequest(
    val foodId: Int,
    val quantity: Int
)