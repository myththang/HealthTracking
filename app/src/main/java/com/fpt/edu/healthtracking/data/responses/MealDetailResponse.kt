package com.fpt.edu.healthtracking.data.responses

data class MealDetailResponse(
    val foodDetails: List<FoodDetail>,
    val imageMealMember: String,
    val mealDate: String,
    val mealPlanId: Int,
    val nameMealPlanMember: String,
    val totalCalories: Int,
    val totalCarb: Double,
    val totalFat: Double,
    val totalProtein: Double
)