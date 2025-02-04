package com.fpt.edu.healthtracking.ui.auth

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.ui.home.HomePageActivity
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.responses.LoginResponse
import com.fpt.edu.healthtracking.databinding.FragmentRegisterBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.ui.infomation.ActivityLevel
import com.fpt.edu.healthtracking.ui.infomation.FitnessGoal
import com.fpt.edu.healthtracking.ui.infomation.Gender
import com.fpt.edu.healthtracking.ui.infomation.InformationViewModel
import com.fpt.edu.healthtracking.util.validator.RegisterValidator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class RegisterFragment : BaseFragment<AuthViewModel, FragmentRegisterBinding, AuthRepository>() {
    private var lastRegisteredEmail: String? = null
    private var lastRegisteredPassword: String? = null

    private lateinit var auth: FirebaseAuth

    override fun getViewModel() = AuthViewModel::class.java
    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRegisterBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)

    private val informationViewModel: InformationViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        setupInputValidation()
    }


    private fun setupUI() {
        binding.apply {
            btnRegister.setOnClickListener {
                if (validateInputs()) {
                    val registerRequest = createRegistrationBody()
                    registerRequest?.let {
                        viewModel.saveRegistrationData(it)
                        val bundle = Bundle().apply {
                            putString("phone_number", binding.etPhone.text.toString())
                            putParcelable("registration_data", it)
                        }
                        findNavController().navigate(R.id.action_registerFragment_to_OTPFragment, bundle)
                    }
                }
            }
        }

    }




    private fun createRegistrationBody(): RegisterRequest? {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val dobString = informationViewModel.dob.value
        val gender = informationViewModel.gender.value
        val height = informationViewModel.height.value ?:170
        val weight = informationViewModel.weight.value ?:70
        val exerciseLevel = informationViewModel.activityLevel.value
        val goal = informationViewModel.fitnessGoal.value

        val dob = try {
            val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dobString?.let {
                val date = originalFormat.parse(it)
                date?.let { targetFormat.format(it) }
            }
        } catch (e: Exception) {
            handleError("Invalid date format")
            null
        }


        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() ||
            dob == null || gender == null || height == null || weight == null ||
            exerciseLevel == null || goal == null) {
            handleError("Please fill in all required fields")

            return null
        }
        Log.d("validate","${RegisterRequest(
            userName = username,
            email = email,
            password = password,
            phoneNumber = phone,
            dob = dob,
            gender = mapGender(gender),
            height = height,
            weight = weight,
            weightPerWeek = informationViewModel.weightChangeRate.value ?: 0f,
            targetWeight = informationViewModel.desiredWeight.value ?: weight,
            exerciseLevel = mapExerciseLevel(exerciseLevel),
            dietId = 1
        )}")
        return RegisterRequest(
            userName = username,
            email = email,
            password = password,
            phoneNumber = phone,
            dob = dob,
            gender = mapGender(gender),
            height = height,
            weight = weight,
            weightPerWeek = informationViewModel.weightChangeRate.value ?: 0f,
            targetWeight = informationViewModel.desiredWeight.value ?: weight,
            exerciseLevel = mapExerciseLevel(exerciseLevel),
            dietId = 1
        )


    }

    private fun mapGender(gender: Gender): Boolean {
        return when (gender) {
            Gender.MALE -> true
            Gender.FEMALE -> false
        }
    }

    private fun mapExerciseLevel(level: ActivityLevel): Int {
        return when (level) {
            ActivityLevel.INACTIVE -> 1
            ActivityLevel.LIGHTLY_ACTIVE -> 2
            ActivityLevel.VERY_ACTIVE -> 3
        }
    }

    private fun mapFitnessGoal(goal: FitnessGoal): String {
        return when (goal) {
            FitnessGoal.LOSE_WEIGHT -> "lose_weight"
            FitnessGoal.MAINTAIN_WEIGHT -> "maintain_weight"
            FitnessGoal.GAIN_WEIGHT -> "gain_weight"
        }
    }


    private fun setupInputValidation() {
        binding.apply {
            etUsername.addTextChangedListener {
                tilUsername.error = RegisterValidator.validateUsername(it.toString())
            }

            etEmail.addTextChangedListener {
                lifecycleScope.launch {
                    val email = it.toString().trim()
                    if (email.isNotEmpty()) {
                        val error = RegisterValidator.validateEmailWithApi(viewModel.getAuthApi(), email)
                        tilEmail.error = error
                    }
                }
            }

            etPhone.addTextChangedListener {
                lifecycleScope.launch {
                    val phone = it.toString().trim()
                    if (phone.isNotEmpty()) {
                        val error = RegisterValidator.validatePhoneWithApi(viewModel.getAuthApi(), phone)
                        tilPhone.error = error
                    }
                }
            }

            etPassword.addTextChangedListener {
                tilPassword.error = RegisterValidator.validatePassword(it.toString())
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            tilUsername.error = RegisterValidator.validateUsername(etUsername.text.toString())
            if (tilUsername.error != null) isValid = false

            tilEmail.error = RegisterValidator.validateEmail(etEmail.text.toString())
            if (tilEmail.error != null) isValid = false

            tilPhone.error = RegisterValidator.validatePhone(etPhone.text.toString())
            if (tilPhone.error != null) isValid = false

            tilPassword.error = RegisterValidator.validatePassword(etPassword.text.toString())
            if (tilPassword.error != null) isValid = false
        }
        return isValid
    }


    private fun setupObservers() {
        viewModel.registrationResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Register Success", Toast.LENGTH_SHORT).show()
                    lastRegisteredEmail?.let { email ->
                        lastRegisteredPassword?.let { password ->
                            viewModel.login(email, password)
                        }
                    }
                }
                is Resource.Failure -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Register Fail", Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.loginResponse.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Success -> {
                    val token = result.value.data.accessToken
                    val weight = informationViewModel.weight.value?.toFloat() ?: 0f
                    val goalType = when (informationViewModel.fitnessGoal.value) {
                        FitnessGoal.GAIN_WEIGHT -> "Tăng cân"
                        FitnessGoal.LOSE_WEIGHT -> "Giảm cân"
                        FitnessGoal.MAINTAIN_WEIGHT -> "Giữ cân"
                        else -> "Giữ cân"
                    }
                    val targetWeight = informationViewModel.desiredWeight.value?.toFloat() ?: weight
                    viewModel.saveUserData(weight, goalType, targetWeight, token)
                }
                is Resource.Failure -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Auto login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.userDataSaveResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Success -> {
                    hideLoading()
                    startActivity(Intent(requireActivity(), HomePageActivity::class.java))
                    requireActivity().finish()
                }
                is Resource.Failure -> {
                    hideLoading()
                    Toast.makeText(requireContext(),
                        "Đã đăng nhập nhưng không thể lưu dữ liệu. Vui lòng cập nhật sau.",
                        Toast.LENGTH_LONG).show()
                    startActivity(Intent(requireActivity(), HomePageActivity::class.java))
                    requireActivity().finish()
                }
            }
        }


    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.btnRegister.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.isVisible = false
        binding.btnRegister.isEnabled = true
    }

    private fun handleSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.text_color))
            .show()
    }
}