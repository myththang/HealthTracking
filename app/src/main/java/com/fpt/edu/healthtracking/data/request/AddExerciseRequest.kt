package com.fpt.edu.healthtracking.data.request

data class AddExerciseRequest(
    val exerciseDiaryId: Int,
    val exerciseId:Int,
    val durationInMinutes: Int,
    val isPractice: Boolean,
    val caloriesBurned: Int
)
data class UpdateExerciseRequest(
    val diaryId:Int,
    val isPractice: Boolean,
    val exerciseDiaryDetailId: Int
)
