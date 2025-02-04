package com.fpt.edu.healthtracking.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.User
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.data.responses.Data
import com.fpt.edu.healthtracking.data.responses.LoginResponse
import com.fpt.edu.healthtracking.data.responses.ObjectResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: AuthRepository

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

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
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials updates LiveData with success`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "Password123!"
        whenever(repository.login(email, password)).thenReturn(Resource.Success(mockLoginResponse))

        // Act
        viewModel.login(email, password)
        testScheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.loginResponse.value
        assertNotNull(result)
        assert(result is Resource.Success)
        assertEquals(mockLoginResponse, (result as Resource.Success).value)
    }

    @Test
    fun `login with invalid credentials updates LiveData with failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrong_password"
        val failureResource = Resource.Failure(false, 401, null)
        whenever(repository.login(email, password)).thenReturn(failureResource)

        // Act
        viewModel.login(email, password)
        testScheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.loginResponse.value
        assertNotNull(result)
        assert(result is Resource.Failure)
        assertEquals(401, (result as Resource.Failure).errorCode)
    }

    @Test
    fun `register with valid data updates LiveData with success`() = runTest {
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

        val mockUser = User(
            username = "testuser",
            email = "test@example.com",
            password = "hashedPassword",
            phone_number = "1234567890"
        )

        //whenever(repository.register(registerRequest)).thenReturn(Resource.Success(mockUser))

        // Act
        viewModel.register()
        testScheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.registrationResult.value
        assertNotNull(result)
        assert(result is Resource.Success)
        assertEquals(mockUser, (result as Resource.Success).value)
    }

    @Test
    fun `saveUserData with valid data updates LiveData with success`() = runTest {
        // Arrange
        val weight = 70f
        val goalType = "maintain_weight"
        val targetWeight = 70f
        val token = "test_token"

        whenever(repository.saveBodyMeasurement(weight, token)).thenReturn(Resource.Success(Unit))
        whenever(repository.saveGoal(goalType, targetWeight, token)).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.saveUserData(weight, goalType, targetWeight, token)
        testScheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.userDataSaveResult.value
        assertNotNull(result)
        assert(result is Resource.Success)
    }
}