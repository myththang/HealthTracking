package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.User
import com.fpt.edu.healthtracking.data.request.LoginRequest
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.data.responses.LoginResponse
import com.fpt.edu.healthtracking.data.responses.Data
import com.fpt.edu.healthtracking.data.responses.ObjectResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {

    @Mock
    private lateinit var api: AuthApi

    @Mock
    private lateinit var preferences: UserPreferences

    private lateinit var repository: AuthRepository

    private val mockObjectResponse = ObjectResponse(
        createdAt = "2024-01-01",
        dietId = 1,
        dob = "1990-01-01",
        email = "test@example.com",
        exerciseLevel = 1,
        gender = true,
        goal = 1,
        height = 170,
        memberId = 1,
        password = "hashedPassword",
        phoneNumber = "1234567890",
        status = true,
        username = "testuser",
        weight = 70
    )

    private val mockLoginResponse = LoginResponse(
        data = Data("test_access_token", "test_refresh_token"),
        message = "Login successful",
        objectResponse = mockObjectResponse,
        success = true
    )

    @Before
    fun setup() {
        repository = AuthRepository(api, preferences)
    }

    @Test
    fun `login with valid credentials returns success`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "Password123!"
        val loginRequest = LoginRequest(email, password)

        whenever(api.login(loginRequest)).thenReturn(mockLoginResponse)

        // Act
        val result = repository.login(email, password)

        // Assert
        assertTrue(result is Resource.Success)
        assertEquals(mockLoginResponse, (result as Resource.Success).value)
        verify(preferences).saveCredentials(email, password)
        verify(preferences).saveAuthTokens("test_access_token", "test_refresh_token")
    }

    @Test
    fun `login with invalid credentials returns failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrong_password"
        val loginRequest = LoginRequest(email, password)

        val errorResponse = Response.error<LoginResponse>(
            401,
            "Unauthorized".toResponseBody(null)
        )
        whenever(api.login(loginRequest)).thenThrow(HttpException(errorResponse))

        // Act
        val result = repository.login(email, password)

        // Assert
        assertTrue(result is Resource.Failure)
        assertEquals(401, (result as Resource.Failure).errorCode)
    }

    @Test
    fun `login with network error returns failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val loginRequest = LoginRequest(email, password)

        whenever(api.login(loginRequest)).thenThrow(IOException("Network error"))

        // Act
        val result = repository.login(email, password)

        // Assert
        assertTrue(result is Resource.Failure)
        assertTrue((result as Resource.Failure).isNetworkError)
    }

    @Test
    fun `register with valid data returns success`() = runTest {
        // Arrange
        val registerRequest = RegisterRequest(
            userName = "testuser",
            email = "test@example.com",
            password = "Password123!",
            dob = "1990-01-01",
            gender = true,
            height = 170,
            weight = 70,
            exerciseLevel = 1,
            phoneNumber = "1234567890",
            weightPerWeek = TODO(),
            targetWeight = TODO(),
            dietId = TODO()
        )

       // whenever(api.register(registerRequest)).thenReturn(mockObjectResponse.toUser())

        // Act
        val result = repository.register(registerRequest)

        // Assert
        assertTrue(result is Resource.Success)
    }

    @Test
    fun `saveBodyMeasurement returns success`() = runTest {
        // Arrange
        val weight = 70f
        val token = "Bearer test_token"
        whenever(api.saveBodyMeasurement(any(), any())).thenReturn(Unit)

        // Act
        val result = repository.saveBodyMeasurement(weight, token)

        // Assert
        assertTrue(result is Resource.Success)
    }

    @Test
    fun `saveGoal returns success`() = runTest {
        // Arrange
        val goalType = "maintain_weight"
        val targetValue = 70f
        val token = "Bearer test_token"
        whenever(api.saveGoal(any(), any())).thenReturn(Unit)

        // Act
        val result = repository.saveGoal(goalType, targetValue, token)

        // Assert
        assertTrue(result is Resource.Success)
    }

    private fun ObjectResponse.toUser() = User(
        username = this.username,
        email = this.email,
        password = this.password,
        phone_number = this.phoneNumber
    )
}