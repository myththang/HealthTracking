package com.fpt.edu.healthtracking.data.model

data class MemberMeal(
    val mealMemberId: Int,
    val image: String,
    val nameMealPlanMember: String,
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarb: Float,
    val totalFat: Float,
    val mealDate: String
)