package com.fpt.edu.healthtracking.data.repository

import android.util.Log
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.request.LoginRequest
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.data.TokenRequest
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.ChangePasswordRequestDTO
import com.fpt.edu.healthtracking.data.request.BodyMeasurementRequest
import com.fpt.edu.healthtracking.data.request.GoalRequest
import com.fpt.edu.healthtracking.data.responses.LoginResponse
import com.fpt.edu.healthtracking.data.responses.ObjectResponse
import com.fpt.edu.healthtracking.data.responses.RefreshTokenResponse
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class AuthRepository(
    val api: AuthApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun sendOTP(phone: String, content: String): Resource<Any> {
        return safeApiCall {
            api.sendOTP(SmsRequest(phones = phone, content = content))
        }
    }

    suspend fun checkAndRestoreSession(): Resource<LoginResponse>? {
        return try {
            Log.d(TAG, "Checking session state...")
            val authState = preferences.authStateFlow.first()

            if (authState.email == null || authState.password == null) {
                Log.d(TAG, "No stored credentials found")
                return null
            }

            if (authState.accessToken != null && authState.refreshToken != null) {
                Log.d(TAG, "Found existing tokens, attempting refresh")

                when (val result = refreshToken(
                    TokenRequest(
                        accessToken = authState.accessToken,
                        refreshToken = authState.refreshToken
                    )
                )) {
                    is Resource.Success -> {
                        if (result.value.success && result.value.data != null) {
                            Log.d(TAG, "Token refresh successful")
                            Resource.Success(LoginResponse(
                                data = result.value.data,
                                message = result.value.message,
                                objectResponse = ObjectResponse(
                                    createdAt = "",
                                    dietId = 1,
                                    dob = "",
                                    email = authState.email,
                                    exerciseLevel = 0,
                                    gender = false,
                                    goal = 1,
                                    height = 0,
                                    memberId = 0,
                                    password = "",
                                    phoneNumber = "",
                                    status = false,
                                    username = "",
                                    weight = 0
                                ),
                                success = true
                            ))
                        } else {
                            Log.d(TAG, "Token refresh failed, attempting login: ${result.value.message}")
                            login(authState.email, authState.password)
                        }
                    }
                    is Resource.Failure -> {
                        Log.d(TAG, "Token refresh failed with error, attempting login")
                        login(authState.email, authState.password)
                    }
                }
            } else {
                Log.d(TAG, "No tokens found, attempting fresh login")
                login(authState.email, authState.password)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during session restore", e)
            preferences.clearAuth()
            null
        }
    }

    suspend fun login(email: String, password: String) = safeApiCall {
        try {
            val loginRequest = LoginRequest(phoneNumber = email, password = password)
            val response = api.login(loginRequest)

            preferences.saveCredentials(email, password)
            preferences.saveAuthTokens(
                response.data.accessToken,
                response.data.refreshToken
            )

            response
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Resource<Unit> {
        return try {
            val response = api.register(registerRequest)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Failure(
                    false,
                    response.code(),
                    response.errorBody()
                )
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    Resource.Failure(false, throwable.code(), throwable.response()?.errorBody())
                }
                else -> {
                    Resource.Failure(true, null, null)
                }
            }
        }
    }

    suspend fun refreshToken(tokenRequest: TokenRequest): Resource<RefreshTokenResponse> {
        return safeApiCall {
            try {
                val response = api.renewToken(tokenRequest)
                
                if (!response.success || response.data == null) {
                    throw Exception("Token refresh failed")
                }

                response.data.let { data ->
                    preferences.saveAuthTokens(
                        data.accessToken,
                        data.refreshToken
                    )
                }
                response
            } catch (e: Exception) {
                preferences.clearAuth() // Xóa token khi refresh thất bại
                throw e
            }
        }
    }


    suspend fun saveBodyMeasurement(weight: Float, token: String) = safeApiCall {
        val request = BodyMeasurementRequest(
            dateChange = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(Date()),
            weight = weight
        )
        api.saveBodyMeasurement(request, "Bearer $token")
    }

    suspend fun updatePassword(newPassword: String,token:String) :Any{
     return   safeApiCall {
            val request = ChangePasswordRequestDTO(
                phoneNumber="0",
                // assuming ChangePasswordRequestDTO has these fields
                newPassword = newPassword
            )
            api.updatePassword(request, "Bearer "+token)
        }
    }


    suspend fun updatePasswordOtp(phoneNumber:String,newPassword: String) : Any{
    return safeApiCall {
        val request = ChangePasswordRequestDTO(
            phoneNumber=phoneNumber,
            // assuming ChangePasswordRequestDTO has these fields
            newPassword = newPassword
        )
        api.updatePasswordOtp(request)
    }}

    suspend fun saveGoal(goalType: String, targetValue: Float, token: String) = safeApiCall {
        val request = GoalRequest(
            goalType = goalType,
            targetValue = targetValue,
            targetDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 days from now
        )
        api.saveGoal(request, "Bearer $token")
    }

    data class SmsRequest(
        val phones: String,
        val content: String
    )
}