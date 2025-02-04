package com.fpt.edu.healthtracking.data.request

data class BodyMeasurementRequest(
    val bodyMeasureId: Int = 0,
    val dateChange: String,
    val weight: Float,
    val bodyFat: Float = 0f,
    val muscles: Float = 0f
)