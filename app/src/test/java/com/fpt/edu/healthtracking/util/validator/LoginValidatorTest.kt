package com.fpt.edu.healthtracking.util.validator

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoginValidatorTest {
    private lateinit var validator: LoginValidator

    @Before
    fun setup() {
        validator = LoginValidator()
    }

    @Test
    fun `empty email returns error`() {
        val result = validator.validateEmail("")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Email không được để trống", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `invalid email format returns error`() {
        val result = validator.validateEmail("invalid.email")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Địa chỉ email không hợp lệ", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `valid email returns success`() {
        val result = validator.validateEmail("test@example.com")
        assertTrue(result is LoginValidator.ValidationResult.Success)
    }

    @Test
    fun `empty password returns error`() {
        val result = validator.validatePassword("")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Mật khẩu không được để trống", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `password less than 8 characters returns error`() {
        val result = validator.validatePassword("Aa1#123")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Mật khẩu cần ít nhất 8 kí tự", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `password without uppercase returns error`() {
        val result = validator.validatePassword("password1#")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Mật khẩu cần ít nhất 1 kí tự viết hoa", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `password without number returns error`() {
        val result = validator.validatePassword("Password#")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Mật khẩu cần ít nhất 1 kí tự số", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `password without special character returns error`() {
        val result = validator.validatePassword("Password123")
        assertTrue(result is LoginValidator.ValidationResult.Error)
        assertEquals("Mật khẩu cần ít nhất 1 kí tự đặc biệt", (result as LoginValidator.ValidationResult.Error).message)
    }

    @Test
    fun `valid password returns success`() {
        val result = validator.validatePassword("Password1#")
        assertTrue(result is LoginValidator.ValidationResult.Success)
    }
}