package com.fpt.edu.healthtracking.data.responses

data class ExerciseListResponseItem(
    val  categoryExercise: String,
    val exerciseId: Int,
    val exerciseImage: String,
    val exerciseName: String
)
data class CardioResponse(
    val exerciseId: Int,
    val typeExercise: Int,
    val exerciseImage: String,
    val categoryExercise: String,
    val exerciseName: String,
    val description: String,
    val minutes1: Int,
    val minutes2: Int,
    val minutes3: Int,
    val calories1: Int,
    val calories2: Int,
    val calories3: Int,
    val metValue: Double,
)

data class ResistanceResponse(
    val exerciseId: Int,
    val typeExercise: Int,
    val exerciseImage: String,
    val categoryExercise: String,
    val exerciseName: String,
    val description: String,
    val reps1: Int,
    val reps2: Int,
    val reps3: Int,
    val sets1: Int,
    val sets2: Int,
    val sets3: Int,
    val minutes1: Int,
    val minutes2: Int,
    val minutes3: Int,
    val metValue: Double
)
data class OtherResponse(
    val exerciseId: Int,
    val typeExercise: Int,
    val exerciseImage: String,
    val categoryExercise: String,
    val exerciseName: String,
    val description: String,
    val metValue: Double
)