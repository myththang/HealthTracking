package com.fpt.edu.healthtracking.data.responses

data class RefreshTokenResponse(
    val data: Data,
    val member: Any,
    val message: String,
    val staff: Any,
    val success: Boolean
)