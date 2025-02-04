package com.fpt.edu.healthtracking.util.validator

import com.fpt.edu.healthtracking.api.AuthApi

object RegisterValidator {
    fun validateUsername(username: String): String? {
        return if (username.isEmpty() || username.length < 3) {
            "Tên cần ít nhất 3 kí tự"
        } else null
    }

    fun validateEmail(email: String): String? {
        return if (email.isEmpty() || !email.contains('@') || !email.contains('.')) {
            "Địa chỉ email không hợp lệ"
        } else null
    }

    fun validatePhone(phone: String): String? {
        return when {
            phone.isEmpty()||
            phone[0] != '0'||
            phone.length != 10 && phone.length != 11||
            phone[1] !in '1'..'9'||
            !phone.all { it.isDigit() }
            -> "Số điện thoại không hợp lệ"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Mật khẩu không được để trống"
            password.length < 8 -> "Mật khẩu cần ít nhất 8 kí tự"
            !password.any { it.isUpperCase() } -> "Mật khẩu cần ít nhất 1 kí tự viết hoa"
            !password.any { it.isDigit() } -> "Mật khẩu cần ít nhất 1 kí tự số"
            !password.any { !it.isLetterOrDigit() } -> "Mật khẩu cần ít nhất 1 kí tự đặc biệt"
            else -> null
        }
    }
    fun validatePasswordsMatch(newPassword:String, confirmPassword:String):String?{
        return when {
            newPassword != confirmPassword -> "Mật khẩu xác nhận không khớp"
            else -> null
        }
    }

    suspend fun validateEmailWithApi(api: AuthApi, email: String): String? {
        val basicValidation = validateEmail(email)
        if (basicValidation != null) return basicValidation

        return try {
            val response = api.checkEmail(email)
            if (response.code() == 400) {
                "Email này đã được sử dụng"
            } else null
        } catch (e: Exception) {
            "Không thể kiểm tra email. Vui lòng thử lại sau"
        }
    }

    suspend fun validatePhoneWithApi(api: AuthApi, phone: String): String? {
        val basicValidation = validatePhone(phone)
        if (basicValidation != null) return basicValidation

        return try {
            val response = api.checkPhone(phone)
            if (response.code() == 400) {
                "Số điện thoại này đã được sử dụng"
            } else null
        } catch (e: Exception) {
            "Không thể kiểm tra số điện thoại. Vui lòng thử lại sau"
        }
    }
}