package com.fpt.edu.healthtracking.data.request

data class GoalRequest(
    val goalType: String,
    val targetValue: Float,
    val targetDate: String
)