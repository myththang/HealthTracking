package com.fpt.edu.healthtracking.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.util.validator.RegisterValidator
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ForgotPasswordTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var repository: AuthRepository

    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }



    @Test
    fun `update password with valid credentials updates LiveData with success`() = runTest {
        // Arrange
        val newPassword = "Password123!"

        //coEvery { repository.updatePassword(newPassword) } returns Resource.Success(Unit)

        // Act
        //viewModel.updatePassword(newPassword)
        testScheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.passwordUpdateResult.value
        TestCase.assertNotNull(result)
        assert(result is Resource.Success<Unit>)
        TestCase.assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun validatePassword_emptyPassword_shouldShowEmptyError() {
        val password = ""
        val error = RegisterValidator.validatePassword(password)

        assertTrue(error != null)
        assertEquals("Mật khẩu không được để trống", error)
    }

    @Test
    fun validatePassword_lessThan8Chars_shouldShowLengthError() {
        val password = "123"
        val error = RegisterValidator.validatePassword(password)

        assertTrue(error != null)
        assertEquals("Mật khẩu cần ít nhất 8 kí tự", error)
    }

    @Test
    fun validatePassword_noUppercase_shouldShowUppercaseError() {
        val password = "password123!"
        val error = RegisterValidator.validatePassword(password)

        assertTrue(error != null)
        assertEquals("Mật khẩu cần ít nhất 1 kí tự viết hoa", error)
    }

    @Test
    fun validatePassword_noDigit_shouldShowDigitError() {
        val password = "Password!"
        val error = RegisterValidator.validatePassword(password)

        assertTrue(error != null)
        assertEquals("Mật khẩu cần ít nhất 1 kí tự số", error)
    }

    @Test
    fun validatePassword_noSpecialChar_shouldShowSpecialCharError() {
        val password = "Password123"
        val error = RegisterValidator.validatePassword(password)

        assertTrue(error != null)
        assertEquals("Mật khẩu cần ít nhất 1 kí tự đặc biệt", error)
    }

    @Test
    fun validatePassword_validPassword_shouldReturnNull() {
        val password = "Password123!"
        val error = RegisterValidator.validatePassword(password)

        assertNull(error)
    }

    @Test
    fun validatePassword_matchingPasswords_shouldReturnNull() {
        val newPassword = "Password123!"
        val confirmPassword = "Password123!"

        val error = RegisterValidator.validatePasswordsMatch(newPassword, confirmPassword)
        assertNull(error)
    }

    @Test
    fun validatePassword_mismatchPasswords_shouldReturnError() {
        val newPassword = "Password123!"
        val confirmPassword = "Password321!"

        val error = RegisterValidator.validatePasswordsMatch(newPassword, confirmPassword)
        assertEquals("Mật khẩu xác nhận không khớp", error)
    }


}