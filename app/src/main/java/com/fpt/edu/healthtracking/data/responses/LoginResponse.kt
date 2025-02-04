package com.fpt.edu.healthtracking.data.responses

data class LoginResponse(
    val data: Data,
    val message: String,
    val objectResponse: ObjectResponse,
    val success: Boolean
)