package com.fpt.edu.healthtracking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.RemoteDataSource
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.AuthState
import com.fpt.edu.healthtracking.data.TokenRequest
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.ui.home.HomePageActivity
import com.fpt.edu.healthtracking.util.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class StartActivity : AppCompatActivity() {
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences = UserPreferences(this)
        authRepository = AuthRepository(
            RemoteDataSource().buildApi(AuthApi::class.java),
            userPreferences
        )

        lifecycleScope.launch {
            handleAppStart()
        }
    }

    private suspend fun handleAppStart() {
        try {
            when (val result = authRepository.checkAndRestoreSession()) {
                is Resource.Success -> {
                    startActivity(Intent(this@StartActivity, HomePageActivity::class.java))
                    finish()
                }
                is Resource.Failure -> {
                    showLoginScreen()
                }
                null -> {
                    showLoginScreen()
                }
            }
        } catch (e: Exception) {
            Log.e("StartActivity", "Error checking session", e)
            showLoginScreen()
        }
    }

    private fun showLoginScreen() {
        setContentView(R.layout.activity_start)
    }
}