package com.fpt.edu.healthtracking.util.validator

import android.util.Patterns

class LoginValidator {
    companion object {
        private val EMAIL_PATTERN = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isEmpty() -> ValidationResult.Error("Email không được để trống")
            !email.matches(EMAIL_PATTERN) -> ValidationResult.Error("Địa chỉ email không hợp lệ")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("Mật khẩu không được để trống")
            password.length < 8 -> ValidationResult.Error("Mật khẩu cần ít nhất 8 kí tự")
            !password.any { it.isUpperCase() } -> ValidationResult.Error("Mật khẩu cần ít nhất 1 kí tự viết hoa")
            !password.any { it.isDigit() } -> ValidationResult.Error("Mật khẩu cần ít nhất 1 kí tự số")
            !password.any { !it.isLetterOrDigit() } -> ValidationResult.Error("Mật khẩu cần ít nhất 1 kí tự đặc biệt")
            else -> ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}