package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.request.LoginRequest
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.data.TokenRequest
import com.fpt.edu.healthtracking.data.model.ChangePasswordRequestDTO
import com.fpt.edu.healthtracking.data.model.User
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.request.BodyMeasurementRequest
import com.fpt.edu.healthtracking.data.request.GoalRequest
import com.fpt.edu.healthtracking.data.responses.LoginResponse
import com.fpt.edu.healthtracking.data.responses.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.PUT

interface AuthApi {

    @POST("Users/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ) : LoginResponse

    @POST("Sms/send")
    suspend fun sendOTP(@Body request: AuthRepository.SmsRequest): Any

    @POST("Users/register-mobile")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<Unit>

    @POST("auth/google")
    suspend fun registerWithGoogle(@Body request: GoogleAuthRequest): AuthResponse

    @POST("Users/RenewToken")
    suspend fun renewToken(@Body request: TokenRequest): RefreshTokenResponse

    @POST("BodyMeasurement")
    suspend fun saveBodyMeasurement(
        @Body request: BodyMeasurementRequest,
        @Header("Authorization") token: String
    ): Any

    @POST("Goal/add-goal")
    suspend fun saveGoal(
        @Body request: GoalRequest,
        @Header("Authorization") token: String
    ): Any

    @GET("Users/check-email")
    suspend fun checkEmail(@Query("email") email: String): Response<Unit>

    @GET("Users/check-phone")
    suspend fun checkPhone(@Query("phoneNumber") phoneNumber: String): Response<Unit>

    @PUT("Users/reset-password")
    suspend fun updatePassword(
        @Body request: ChangePasswordRequestDTO,
        @Header("Authorization") token: String
    )

    @PUT("Users/reset-password-otp")
    suspend fun updatePasswordOtp(
        @Body request: ChangePasswordRequestDTO
    )

    data class GoogleAuthRequest(
        val idToken: String
    )

    data class AuthResponse(
        val token: String,
        val user: User
    )
}