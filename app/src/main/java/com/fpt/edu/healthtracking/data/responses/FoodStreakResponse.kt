package com.fpt.edu.healthtracking.data.responses

data class FoodStreakResponse(
    val dates: List<String>,
    val streakNumber: Int
)

data class ExerciseStreakResponse(
    val streakCount: Int,
    val streakDates: List<String>
)