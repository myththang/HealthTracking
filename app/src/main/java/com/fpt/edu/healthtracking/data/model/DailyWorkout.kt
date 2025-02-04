package com.fpt.edu.healthtracking.data.model

data class DailyWorkout(
    val id: Int,
    val exercise: Exercise,
    val date: String,
    var isCompleted: Boolean = false,
    var distance: Float? = null,
    var duration: Int? = null
)