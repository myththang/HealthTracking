package com.fpt.edu.healthtracking.data.responses

data class ObjectResponse(
    val createdAt: String,
    val dietId: Any,
    val dob: String,
    val email: String,
    val exerciseLevel: Int,
    val gender: Boolean,
    val goal: Any,
    val height: Int,
    val memberId: Int,
    val password: String,
    val phoneNumber: String,
    val status: Boolean,
    val username: String,
    val weight: Int
)