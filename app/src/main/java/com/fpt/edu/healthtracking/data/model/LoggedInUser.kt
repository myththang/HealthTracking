package com.fpt.edu.healthtracking.data.model


/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class User(
    val username: String,
    val email: String,
    val password: String,
    val phone_number: String
)