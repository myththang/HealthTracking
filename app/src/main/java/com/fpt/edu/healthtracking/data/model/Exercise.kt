package com.fpt.edu.healthtracking.data.model

data class Exercise(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val category: ExerciseCategory,
    var distance: Float? = null, // For distance-based exercises like running, cycling
    var duration: Int? = null    // Duration in minutes
)
enum class ExerciseCategory {
    CARDIO,
    STRENGTH,
    ALL,
    OTHER
}
