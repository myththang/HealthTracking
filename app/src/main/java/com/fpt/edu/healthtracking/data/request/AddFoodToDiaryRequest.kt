package com.fpt.edu.healthtracking.data.request

data class AddFoodToDiaryRequest(
    val diaryId: Int,
    val foodId: Int,
    val quantity: Int,
    val mealType: Int
)