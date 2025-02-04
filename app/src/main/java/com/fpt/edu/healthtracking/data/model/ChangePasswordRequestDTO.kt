package com.fpt.edu.healthtracking.data.model

data class ChangePasswordRequestDTO(
    val phoneNumber: String,
    val newPassword: String
)
