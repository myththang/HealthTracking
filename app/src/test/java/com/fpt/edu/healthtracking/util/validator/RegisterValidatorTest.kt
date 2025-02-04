package com.fpt.edu.healthtracking.util.validator

import org.junit.Assert.*
import org.junit.Test

class RegisterValidatorTest {
    @Test
    fun `validate valid username`() {
        val result = RegisterValidator.validateUsername("John")
        assertNull(result)
    }

    @Test
    fun `validate invalid username`() {
        val result = RegisterValidator.validateUsername("Jo")
        assertEquals("Tên cần ít nhất 3 kí tự", result)
    }

    @Test
    fun `validate empty username`() {
        val result = RegisterValidator.validateUsername("")
        assertEquals("Tên cần ít nhất 3 kí tự", result)
    }

    @Test
    fun `validate valid email`() {
        val result = RegisterValidator.validateEmail("test@example.com")
        assertNull(result)
    }

    @Test
    fun `validate invalid email`() {
        val result = RegisterValidator.validateEmail("invalidEmail.com")
        assertEquals("Địa chỉ email không hợp lệ", result)
    }

    @Test
    fun `validate empty email`() {
        val result = RegisterValidator.validateEmail("")
        assertEquals("Địa chỉ email không hợp lệ", result)
    }

    @Test
    fun `validate valid 10-digit phone number`() {
        val result = RegisterValidator.validatePhone("0901234567")
        assertNull(result)
    }

    @Test
    fun `validate valid 11-digit phone number`() {
        val result = RegisterValidator.validatePhone("01234567890")
        assertNull(result)
    }

    @Test
    fun `validate phone number without starting zero`() {
        val result = RegisterValidator.validatePhone("901234567")
        assertEquals("Số điện thoại không hợp lệ", result)
    }

    @Test
    fun `validate phone number with invalid prefix`() {
        val result = RegisterValidator.validatePhone("0012345678")
        assertEquals("Số điện thoại không hợp lệ", result)
    }

    @Test
    fun `validate phone number with non-digit characters`() {
        val result = RegisterValidator.validatePhone("09012345ab")
        assertEquals("Số điện thoại không hợp lệ", result)
    }

    @Test
    fun `validate empty phone number`() {
        val result = RegisterValidator.validatePhone("")
        assertEquals("Số điện thoại không hợp lệ", result)
    }

    @Test
    fun `validate phone number with incorrect length`() {
        val result = RegisterValidator.validatePhone("0123456")
        assertEquals("Số điện thoại không hợp lệ", result)
    }

    @Test
    fun `validate valid password`() {
        val result = RegisterValidator.validatePassword("Password1!")
        assertNull(result)
    }

    @Test
    fun `validate password with no uppercase`() {
        val result = RegisterValidator.validatePassword("password1!")
        assertEquals("Mật khẩu cần ít nhất 1 kí tự viết hoa", result)
    }

    @Test
    fun `validate password with no digit`() {
        val result = RegisterValidator.validatePassword("Password!")
        assertEquals("Mật khẩu cần ít nhất 1 kí tự số", result)
    }

    @Test
    fun `validate password with no special character`() {
        val result = RegisterValidator.validatePassword("Password1")
        assertEquals("Mật khẩu cần ít nhất 1 kí tự đặc biệt", result)
    }

    @Test
    fun `validate short password`() {
        val result = RegisterValidator.validatePassword("Pass1!")
        assertEquals("Mật khẩu cần ít nhất 8 kí tự", result)
    }

    @Test
    fun `validate empty password`() {
        val result = RegisterValidator.validatePassword("")
        assertEquals("Mật khẩu không được để trống", result)
    }

    @Test
    fun `validate all phone number edge cases`() {
        val testCases = mapOf(
            "" to "Số điện thoại không hợp lệ",
            "123456789" to "Số điện thoại không hợp lệ",
            "0123456" to "Số điện thoại không hợp lệ",
            "012345678901" to "Số điện thoại không hợp lệ",
            "0abcdefghij" to "Số điện thoại không hợp lệ",
            "0023456789" to "Số điện thoại không hợp lệ",
            "0123456789" to null,
            "01234567890" to null
        )

        testCases.forEach { (input, expected) ->
            val result = RegisterValidator.validatePhone(input)
            assertEquals("Testing phone number: $input", expected, result)
        }
    }
}