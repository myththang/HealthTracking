package com.fpt.edu.healthtracking.util

object OTPHelper {
    fun generateOTP(): String {
        return (100000..999999).random().toString()
    }
}