package com.fpt.edu.healthtracking.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.databinding.FragmentPhoneNumberBinding
import com.fpt.edu.healthtracking.databinding.FragmentRegisterBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.validator.RegisterValidator
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [PhoneNumberFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PhoneNumberFragment : BaseFragment<AuthViewModel, FragmentPhoneNumberBinding, AuthRepository>() {
    override fun getViewModel()= AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )=  FragmentPhoneNumberBinding.inflate(inflater, container, false)

    override fun getFragmentRepository()=AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Send OTP when button is clicked
        binding.btnSendOtp.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()

            // Validate phone number input before proceeding
            if (validatePhoneNumber(phoneNumber)) {
                // Call the suspend function to check the phone number asynchronously
                lifecycleScope.launch {
                    val validationError = RegisterValidator.validatePhoneWithApi(viewModel.getAuthApi(), phoneNumber)

                    if (validationError.equals("Số điện thoại này đã được sử dụng")) {
                        // Phone number is valid and doesn't exist, proceed to send OTP
                        sendOtp(phoneNumber)
                    } else {
                        // Show error if the phone number exists or any validation error occurs
                        binding.etPhoneNumber.error = "Số điện thoại chưa được đăng ký"
                    }
                }
            } else {
                // Show error if phone number format is invalid
                binding.etPhoneNumber.error = "Số điện thoại không hợp lệ"
            }
        }

    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotEmpty() && phoneNumber.length == 10 // Example validation logic
    }

    private fun sendOtp(phoneNumber: String) {
        // Implement the logic to send the OTP here
        // For now, just log the phone number to test navigation
        navigateToOtpFragment(phoneNumber)
    }

    private fun navigateToOtpFragment(phoneNumber: String) {
        // Navigate to OTP Fragment
        val bundle = bundleOf(
            "phone_number" to phoneNumber,
            "forget_password" to "forget_password"
        )
        findNavController().navigate(R.id.action_phoneNumberFragment_to_OTPFragment, bundle)
    }
}