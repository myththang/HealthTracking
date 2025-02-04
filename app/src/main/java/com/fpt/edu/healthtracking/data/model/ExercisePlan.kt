package com.fpt.edu.healthtracking.data.model

data class ExercisePlan(
    val exercisePlanId: Int,
    val exercisePlanImage: String?,
    val name: String,
    val totalCaloriesBurned: Int?
)

data class ExercisePlanDetail(
    val exercisePlanId: Int,
    val exercisePlanImage: String?,
    val name: String,
    val totalCaloriesBurned: Int?,
    val details: List<ExercisePlanDetailItem>
)

data class ExercisePlanDetailItem(
    val exercisePlanDetailId: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val day: Int,
    val duration: Int
)

data class AssignPlanRequest(
    val planId: Int,
    val startDate: String
)

data class ExerciseItem(
    val exerciseId: Int,
    val name: String,
    val description: String,
    val image: String,
    val duration: Int,
    val calories: Int,
    val sets: Int,
    val reps: Int,
    val restTime: Int
)