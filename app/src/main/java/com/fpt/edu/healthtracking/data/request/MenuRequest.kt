package com.fpt.edu.healthtracking.data.request

data class AddMenuPlanDayRequest(
    val selectDate: String,
    val day: Int,
    val mealPlanId: Int
)

data class AddMenuPlanMealRequest(
    val mealTypeDay: Int,
    val selectMealTypeToAdd: Int,
    val selectDateToAdd: String,
    val day: Int,
    val mealPlanId: Int
)