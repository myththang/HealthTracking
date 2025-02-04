package com.fpt.edu.healthtracking.data.request

data class AddMealRequest(
    val diaryId: Int,
    val mealMemberId: Int,
    val mealType: Int
)