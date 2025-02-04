package com.fpt.edu.healthtracking.data.responses

data class ExerciseDiaryResponse(
    val exerciseDiaryId: Int,
    val memberId: Int,
    val exercisePlanId: Int?,
    val date: String,
    val totalDuration: Float,
    val totalCaloriesBurned: Float,
    val exerciseDiaryDetails: List<ExerciseDiaryDetail>
)

data class ExerciseDiaryDetail(
    val exerciseDiaryDetailsId: Int,
    val exerciseDiaryId: Int,
    var isPractice: Boolean,
    val exerciseId: Int,
    val exerciseName: String,
    val exerciseImage: String,
    var duration: Float,
    val caloriesBurned: Float
)