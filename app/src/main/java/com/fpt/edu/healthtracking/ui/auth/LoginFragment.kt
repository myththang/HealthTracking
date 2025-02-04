package com.fpt.edu.healthtracking.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.ui.home.HomePageActivity
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.databinding.FragmentLoginBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.validator.LoginValidator

class LoginFragment : BaseFragment<AuthViewModel, FragmentLoginBinding, AuthRepository>() {
    private val loginValidator = LoginValidator()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.apply {
            btnLogin.setOnClickListener {
                hideError()
                if (validateInputs()) {
                    val phone = etPhone.text.toString().trim()
                    val password = etPassword.text.toString().trim()
                    showLoading(true)
                    viewModel.login(phone, password)
                }
            }

            tvForgotPassword.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_phonenumber)
            }

            tvSignUp.setOnClickListener {
                findNavController().navigate(R.id.activityLevelFragment)
            }
        }
    }

    private fun validateInputs(): Boolean {
        binding.apply {
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var isValid = true

            if (phone.isEmpty() || phone.length < 10) {
                tilPhone.error = "Vui lòng nhập số điện thoại hợp lệ"
                isValid = false
            } else {
                tilPhone.error = null
            }

            if (password.isEmpty()) {
                tilPassword.error = "Vui lòng nhập mật khẩu"
                isValid = false
            } else {
                tilPassword.error = null
            }

            return isValid
        }
    }

    private fun showError(message: String) {
        binding.apply {
            tvError.text = message
            cvError.isVisible = true
        }
    }

    private fun hideError() {
        binding.cvError.isVisible = false
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            btnLogin.isEnabled = !isLoading
            btnLogin.text = if (isLoading) "" else "Đăng nhập"
        }
    }

    private fun setupObservers() {
        viewModel.loginResponse.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            when (result) {
                is Resource.Success -> {
                    startActivity(Intent(requireActivity(), HomePageActivity::class.java))
                    requireActivity().finish()
                }
                is Resource.Failure -> handleLoginError(result)
            }
        }
    }

    private fun handleLoginError(failure: Resource.Failure) {
        val errorMessage = when {
            failure.isNetworkError -> "Vui lòng kiểm tra kết nối mạng"
            failure.errorCode == 401 -> "Số điện thoại hoặc mật khẩu không chính xác"
            failure.errorCode == 403 -> "Tài khoản của bạn đã bị khóa"
            else -> "Đã có lỗi xảy ra. Vui lòng thử lại sau"
        }
        showError(errorMessage)
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)
}