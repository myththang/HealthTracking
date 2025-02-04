package com.fpt.edu.healthtracking.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RegisterRequest(
    val userName: String,
    val email: String,
    val password: String,
    val dob: String,
    val gender: Boolean,
    val height: Int,
    val weight: Int,
    val weightPerWeek: Float,
    val targetWeight: Int,
    val dietId: Int = 1,
    val exerciseLevel: Int,
    val phoneNumber: String
) : Parcelable