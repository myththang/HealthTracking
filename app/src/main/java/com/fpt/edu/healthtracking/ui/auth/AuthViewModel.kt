package com.fpt.edu.healthtracking.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.request.LoginRequest
import com.fpt.edu.healthtracking.data.responses.LoginResponse
import com.fpt.edu.healthtracking.util.OTPHelper
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private var generatedOTP: String? = null
    private var lastOTPSentTimestamp: Long = 0
    private val OTP_COOLDOWN = 60000L

    private val _otpVerification = MutableLiveData<Resource<Boolean>>()
    val otpVerification: LiveData<Resource<Boolean>> = _otpVerification

    private val _loginResponse : MutableLiveData<Resource<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<LoginResponse>>

        get() = _loginResponse

    private val _registrationResult = MutableLiveData<Resource<Any>>()
    val registrationResult: LiveData<Resource<Any>> = _registrationResult

    private val _userDataSaveResult = MutableLiveData<Resource<Unit>>()
    val userDataSaveResult: LiveData<Resource<Unit>> = _userDataSaveResult

    private val _otpSent = MutableLiveData<Resource<Any>>()
    val otpSent: LiveData<Resource<Any>> = _otpSent

    private val _registrationData = MutableLiveData<RegisterRequest>()
    val registrationData: LiveData<RegisterRequest> = _registrationData

    private val _passwordUpdateResult = MutableLiveData<Resource<Unit>>()
    val passwordUpdateResult: LiveData<Resource<Unit>> = _passwordUpdateResult

    fun saveRegistrationData(data: RegisterRequest) {
        _registrationData.value = data
    }

    private fun canSendOTP(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastOTPSentTimestamp) >= OTP_COOLDOWN
    }

    fun sendOTP(phone: String) {
        if (!canSendOTP()) {
            _otpSent.value = Resource.Failure(false, 429, null) // Too many requests
            return
        }

        viewModelScope.launch {
            try {
                val otp = OTPHelper.generateOTP()
                generatedOTP = otp
                val content = "Mã OTP của bạn là: $otp"
                val result = repository.sendOTP(phone, content)
                if (result is Resource.Success) {
                    lastOTPSentTimestamp = System.currentTimeMillis()
                }
                _otpSent.value = result
            } catch (e: Exception) {
                _otpSent.value = Resource.Failure(true, null, null)
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _registrationData.value?.let { request ->
                try {
                    val result = repository.register(request)
                    _registrationResult.value = result

                    if (result is Resource.Success) {
                        val loginResult = repository.login(request.phoneNumber, request.password)
                        _loginResponse.value = loginResult
                    }
                } catch (e: Exception) {
                    _registrationResult.value = Resource.Failure(true, null, null)
                }
            }
        }
    }


    fun verifyOTP(inputOTP: String): Boolean {
        return generatedOTP == inputOTP
    }


    fun login(email: String, password: String) = viewModelScope.launch {
        try {
            Log.d("LoginDebug", "Starting login request")
            Log.d("LoginDebug", "Email: $email")
            Log.d("LoginDebug", "Password length: ${password.length}")

            val result = repository.login(email, password)

            when (result) {
                is Resource.Success -> {
                    Log.d("LoginDebug", "Login successful")
                    Log.d("LoginDebug", "Response: ${result.value}")
                }
                is Resource.Failure -> {
                    Log.e("LoginDebug", "Login failed")
                    Log.e("LoginDebug", "Is Network Error: ${result.isNetworkError}")
                    Log.e("LoginDebug", "Error Code: ${result.errorCode}")
                    Log.e("LoginDebug", "Error Body: ${result.errorBody?.string()}")
                }
            }

            _loginResponse.value = result

        } catch (e: Exception) {
            Log.e("LoginDebug", "Exception during login", e)
            Log.e("LoginDebug", "Exception type: ${e.javaClass.simpleName}")
            Log.e("LoginDebug", "Exception message: ${e.message}")
            Log.e("LoginDebug", "Exception cause: ${e.cause}")

            _loginResponse.value = Resource.Failure(
                isNetworkError = e is IOException,
                errorCode = null,
                errorBody = null
            )
        }
    }

    fun saveUserData(weight: Float, goalType: String, targetWeight: Float, token: String) = viewModelScope.launch {
        try {
            when (val bodyResult = repository.saveBodyMeasurement(weight, token)) {
                is Resource.Success -> {
                    when (val goalResult = repository.saveGoal(goalType, targetWeight, token)) {
                        is Resource.Success -> {
                            _userDataSaveResult.value = Resource.Success(Unit)
                        }
                        is Resource.Failure -> {
                            _userDataSaveResult.value = goalResult
                        }
                    }
                }
                is Resource.Failure -> {
                    _userDataSaveResult.value = bodyResult
                }
            }
        } catch (e: Exception) {
            _userDataSaveResult.value = Resource.Failure(true, null, null)
        }
    }

    fun loginAfterRegistration(email: String, password: String) {
        viewModelScope.launch {
            try {
                LoginRequest(email, password)
                val result = repository.login(email,password)
                _loginResponse.value = result
            } catch (e: Exception) {
                _loginResponse.value = Resource.Failure(true, null, null)
            }
        }
    }

    fun getAuthApi(): AuthApi {
        return repository.api
    }


     fun updatePassword(newPassword: String,token: String)= viewModelScope.launch {
        try {
            // Call the API to update the password
            when (val result = repository.updatePassword(newPassword,token)) {
                is Resource.Success<*> -> {
                    // If password update is successful
                    _passwordUpdateResult.value = Resource.Success(Unit)
                }
                is Resource.Failure -> {
                    // If there is a failure, propagate the error
                    _passwordUpdateResult.value = result
                }
            }
        } catch (e: Exception) {
            // Handle unexpected exceptions
            _passwordUpdateResult.value = Resource.Failure(true, null, null)
        }

    }

    fun updatePasswordOtp(phone: String, newPassword: String)= viewModelScope.launch {
        try {
            val result = repository.updatePasswordOtp(phone, newPassword)
            Log.d("PasswordUpdate", "Got result: $result")
            // Call the API to update the password
            when (result) {
                is Resource.Success<*> -> {
                    // If password update is successful
                    _passwordUpdateResult.value = Resource.Success(Unit)
                }
                is Resource.Failure -> {
                    // If there is a failure, propagate the error
                    _passwordUpdateResult.value = result
                }
            }
        } catch (e: Exception) {
            // Handle unexpected exceptions
            _passwordUpdateResult.value = Resource.Failure(true, null, null)
        }

    }
}



