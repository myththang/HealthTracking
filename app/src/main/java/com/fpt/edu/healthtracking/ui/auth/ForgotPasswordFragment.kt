package com.fpt.edu.healthtracking.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.ProfileApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.databinding.FragmentAccountDetailBinding
import com.fpt.edu.healthtracking.databinding.FragmentForgotPasswordBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.ui.profile.AccountDetailViewModel
import com.fpt.edu.healthtracking.util.validator.RegisterValidator
import kotlinx.coroutines.launch

class ForgotPassword :
    BaseFragment<AuthViewModel, FragmentForgotPasswordBinding, AuthRepository>() {


    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentForgotPasswordBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): AuthRepository {
        val api = remoteDataSource.buildApi(AuthApi::class.java)
        return AuthRepository(
            api = api,
            preferences = userPreferences
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        binding.btnResetPassword.setOnClickListener {
            validateAndResetPassword()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            tilPassword.error = RegisterValidator.validatePassword(etPassword.text.toString())
            if (tilPassword.error != null) isValid = false
        }
        return isValid
    }
    lateinit var newPassword :String
     var phone :String? = null
    private fun validateAndResetPassword() {
        // Get the password and confirm password from the input fields
         newPassword = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate inputs (assuming you have some other validation logic)
        if (!validateInputs()) {
            return
        }
        var error = RegisterValidator.validatePasswordsMatch(newPassword,confirmPassword)
        // Check if passwords match
        if (error!=null) {
            binding.tilConfirmPassword.error = error
            return
        } else {
            binding.tilConfirmPassword.error = null
        }

        // Clear error messages if validation passes
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null


        lifecycleScope.launch {
             phone =arguments?.getString("phone_number")
            if (phone != null) {
                // OTP flow: If the phone number is available, update the password via OTP
                viewModel.updatePasswordOtp(phone!!, newPassword)
            } else {
                // Token-based flow: Check if an access token is available
                userPreferences.authStateFlow.collect { authState ->
                    val hasAccessToken = authState.accessToken != null

                    // Use token-based password update if an access token is available
                    if (hasAccessToken) {
                        authState.accessToken?.let { token ->
                            viewModel.updatePassword(newPassword,token)
                        }
                    } else {
                        // Handle the case where neither accessToken nor OTP verification is available
                        Toast.makeText(
                            context,
                            "Please ensure you're authenticated or request an OTP for password reset.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }

    }

    private fun observeViewModel() {
        // Observe the password update result
        viewModel.passwordUpdateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    // Handle success (e.g., show a success message or update the UI)
                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT)
                        .show()
                    lifecycleScope.launch {
                        userPreferences.clearAuth()
                    }
                    findNavController().navigate(R.id.loginFragment)
                }
                is Resource.Failure -> {
                    // Handle failure (e.g., show error message)
                    Toast.makeText(
                        context,
                        "Password update failed: ${result.errorCode}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}