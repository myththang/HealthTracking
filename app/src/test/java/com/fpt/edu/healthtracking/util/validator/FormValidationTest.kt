package com.fpt.edu.healthtracking.util.validator

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FormValidationTest {

    data class RegistrationForm(
        val username: String,
        val email: String,
        val phone: String,
        val password: String
    )

    data class LoginForm(
        val email: String,
        val password: String
    )

    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String?>
    )

    @Test
    fun `test complete valid registration form`() {
        val form = RegistrationForm(
            username = "NguyenVanA",
            email = "nguyenvana@gmail.com",
            phone = "0901234567",
            password = "Matkhau123!"
        )

        val result = validateRegistrationForm(form)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `test registration form with all fields invalid`() {
        val form = RegistrationForm(
            username = "a",
            email = "invalidemail.com",
            phone = "123",
            password = "yeu"
        )

        val result = validateRegistrationForm(form)

        assertFalse(result.isValid)
        assertEquals(4, result.errors.size)
        assertEquals("Tên cần ít nhất 3 kí tự", result.errors["username"])
        assertEquals("Địa chỉ email không hợp lệ", result.errors["email"])
        assertEquals("Số điện thoại không hợp lệ", result.errors["phone"])
        assertEquals("Mật khẩu cần ít nhất 8 kí tự", result.errors["password"])
    }

    @Test
    fun `test registration form with mixed valid and invalid fields`() {
        val form = RegistrationForm(
            username = "XuanThang",
            email = "email.sai",
            phone = "0908765432",
            password = "matkhau"
        )

        val result = validateRegistrationForm(form)

        assertFalse(result.isValid)
        assertEquals(2, result.errors.size)
        assertNull(result.errors["username"])
        assertEquals("Địa chỉ email không hợp lệ", result.errors["email"])
        assertNull(result.errors["phone"])
        assertEquals("Mật khẩu cần ít nhất 8 kí tự", result.errors["password"])
    }

    @Test
    fun `test complete valid login form`() {
        val form = LoginForm(
            email = "nguyenvanb@mail.com",
            password = "Matkhau123!"
        )

        val result = validateLoginForm(form)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `test login form with all fields invalid`() {
        val form = LoginForm(
            email = "saiemail",
            password = "123"
        )

        val result = validateLoginForm(form)

        assertFalse(result.isValid)
        assertEquals(2, result.errors.size)
        assertEquals("Địa chỉ email không hợp lệ", result.errors["email"])
        assertEquals("Mật khẩu cần ít nhất 8 kí tự", result.errors["password"])
    }

    private fun validateRegistrationForm(form: RegistrationForm): ValidationResult {
        val errors = mutableMapOf<String, String?>()

        RegisterValidator.validateUsername(form.username)?.let {
            errors["username"] = it
        }

        RegisterValidator.validateEmail(form.email)?.let {
            errors["email"] = it
        }

        RegisterValidator.validatePhone(form.phone)?.let {
            errors["phone"] = it
        }

        RegisterValidator.validatePassword(form.password)?.let {
            errors["password"] = it
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    private fun validateLoginForm(form: LoginForm): ValidationResult {
        val errors = mutableMapOf<String, String?>()

        when (val emailResult = LoginValidator().validateEmail(form.email)) {
            is LoginValidator.ValidationResult.Error -> errors["email"] = emailResult.message
            is LoginValidator.ValidationResult.Success -> { }
        }

        when (val passwordResult = LoginValidator().validatePassword(form.password)) {
            is LoginValidator.ValidationResult.Error -> errors["password"] = passwordResult.message
            is LoginValidator.ValidationResult.Success -> { }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    @Test
    fun `test registration form with various password patterns`() {
        val testCases = listOf(
            "matkhau" to "Mật khẩu cần ít nhất 8 kí tự",
            "matkhaumoi" to "Mật khẩu cần ít nhất 1 kí tự viết hoa",
            "MatkhauMoi" to "Mật khẩu cần ít nhất 1 kí tự số",
            "Matkhau1" to "Mật khẩu cần ít nhất 1 kí tự đặc biệt",
            "Matkhau1!" to null
        )

        testCases.forEach { (password, expectedError) ->
            val form = RegistrationForm(
                username = "XuanThang",
                email = "XuanThang@mail.com",
                phone = "0901234567",
                password = password
            )

            val result = validateRegistrationForm(form)

            if (expectedError != null) {
                assertEquals(expectedError, result.errors["password"])
                assertFalse(result.isValid)
            } else {
                assertNull(result.errors["password"])
                assertTrue(result.isValid)
            }
        }
    }

    @Test
    fun `test registration form with various phone number patterns`() {
        val testCases = listOf(
            "123456789" to "Số điện thoại không hợp lệ",
            "091234567" to "Số điện thoại không hợp lệ",
            "09234" to "Số điện thoại không hợp lệ",
            "0901234567" to null,
            "0912345678" to null,
            "0123456789" to null,
            "01234567890" to "Số điện thoại không hợp lệ",
            "" to "Số điện thoại không hợp lệ"
        )

        testCases.forEach { (phone, expectedError) ->
            val form = RegistrationForm(
                username = "XuanThang",
                email = "XuanThang@mail.com",
                phone = phone,
                password = "Matkhau1!"
            )

            val result = validateRegistrationForm(form)

            if (expectedError != null) {
                assertEquals(expectedError, result.errors["phone"])
                assertFalse(result.isValid)
            } else {
                assertNull(result.errors["phone"])
                assertTrue(result.isValid)
            }
        }
    }

    @Test
    fun `test registration form with various email patterns`() {
        val testCases = listOf(
            "saiemail" to "Địa chỉ email không hợp lệ",
            "email@" to "Địa chỉ email không hợp lệ",
            "email@sai" to "Địa chỉ email không hợp lệ",
            "dungemail@mail.com" to null,
            "dung.email+tag@mail.com" to null
        )

        testCases.forEach { (email, expectedError) ->
            val form = RegistrationForm(
                username = "XuanThang",
                email = email,
                phone = "0901234567",
                password = "Matkhau1!"
            )

            val result = validateRegistrationForm(form)

            if (expectedError != null) {
                assertEquals(expectedError, result.errors["email"])
                assertFalse(result.isValid)
            } else {
                assertNull(result.errors["email"])
                assertTrue(result.isValid)
            }
        }
    }
}
