package com.fpt.edu.healthtracking.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.KeyEvent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.request.RegisterRequest
import com.fpt.edu.healthtracking.databinding.FragmentOTPBinding
import com.fpt.edu.healthtracking.databinding.FragmentRegisterBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.ui.home.HomePageActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.fpt.edu.healthtracking.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OTPFragment : BaseFragment<AuthViewModel, FragmentOTPBinding, AuthRepository>() {
    private var countDownTimer: CountDownTimer? = null
    private var phoneNumber: String = ""
    private var registrationData: RegisterRequest? = null

    private val loadingMessages = listOf(
        "Đang khởi tạo hồ sơ sức khỏe của bạn...",
        "Đang tính toán chỉ số dinh dưỡng phù hợp...",
        "Đang chuẩn bị kế hoạch bữa ăn cá nhân hóa...",
        "Đang tối ưu gợi ý thực đơn cho bạn...",
        "Sẵn sàng bắt đầu hành trình sống khỏe cùng bạn!"
    )
    private var currentMessageIndex = 0
    private var messageChangeHandler: Handler? = null
    private val messageChangeRunnable = object : Runnable {
        override fun run() {
            binding.tvLoadingMessage.apply {
                alpha = 1f
                animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction {
                        text = loadingMessages[currentMessageIndex]
                        animate()
                            .alpha(1f)
                            .setDuration(500)
                            .start()
                    }
                    .start()
            }
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
            messageChangeHandler?.postDelayed(this, 3000)
        }
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentOTPBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            phoneNumber = it.getString("phone_number", "")
            registrationData = it.getParcelable("registration_data")
        }

        setupUI()
        setupObservers()
        startTimer()
        viewModel.sendOTP(phoneNumber)


    }

    private fun setupUI() {
        binding.apply {
            tvPhoneNumber.text = phoneNumber

            setupOtpInputs()

            btnVerify.apply {
                isEnabled = true
                setOnClickListener {

                    val otpCode = getOtpCode()
                    if (otpCode.length == 6) {
                        Toast.makeText(
                        context,
                        "OTP Verified!",
                        Toast.LENGTH_SHORT
                    ).show()
                        if(viewModel.verifyOTP(otpCode)){
                            if (arguments?.getString("forget_password") == "forget_password") {

                                findNavController().navigate(R.id.action_OTPFragment_to_forgotFragment,
                                    bundleOf("opt" to "otp",
                                        "phone_number" to phoneNumber
                                    ))
                            }
                            else{
                                registrationData?.let { data ->
                                    viewModel.saveRegistrationData(data)
                                    viewModel.register()
                                }
                        }
                        } else {
                            Toast.makeText(context, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            btnResend.isEnabled = false

            btnResend.setOnClickListener {
                if (btnResend.isEnabled) {
                    viewModel.sendOTP(phoneNumber)
                    startTimer()
                    btnResend.isEnabled = false
                }
            }

            btnChangePhone.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupOtpInputs() {
        val editTexts = with(binding) {
            listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)
        }

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                    checkOtpComplete()
                }
            })

            editTexts[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editTexts[i].text.isEmpty() && i > 0) {
                        editTexts[i - 1].requestFocus()
                        editTexts[i - 1].text.clear()
                        checkOtpComplete()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun checkOtpComplete() {
        val otpCode = getOtpCode()
        binding.btnVerify.isEnabled = otpCode.length == 6
    }

    private fun getOtpCode(): String {
        return with(binding) {
            "${etOtp1.text}${etOtp2.text}${etOtp3.text}${etOtp4.text}${etOtp5.text}${etOtp6.text}"
        }
    }

    private fun clearOtpInputs() {
        with(binding) {
            etOtp1.text.clear()
            etOtp2.text.clear()
            etOtp3.text.clear()
            etOtp4.text.clear()
            etOtp5.text.clear()
            etOtp6.text.clear()
            etOtp1.requestFocus()
        }
    }

    private fun setupObservers() {
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    showLoading()
                    registrationData?.let { data ->
                        viewModel.loginAfterRegistration(data.phoneNumber, data.password)
                    }
                }
                is Resource.Failure -> {
                    hideLoading()
                    Toast.makeText(context, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.otpSent.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(context, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> {
                    hideLoading()
                    if (result.errorCode == 429) {
                        Toast.makeText(context, "Vui lòng đợi trước khi gửi lại mã OTP", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Không thể gửi mã OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.loginResponse.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    val sharedPreferences = context?.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                    val editor = sharedPreferences?.edit()
                    editor?.putString("last_weight_change_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        Date()
                    ))
                    editor?.apply()
                    showLoading("Đang chuẩn bị ứng dụng...")
                    Handler(Looper.getMainLooper()).postDelayed({
                        hideLoading()
                        startActivity(Intent(requireActivity(), HomePageActivity::class.java))
                        requireActivity().finish()
                    }, 1500) // Delay 1.5s để hiển thị loading
                }
                is Resource.Failure -> {
                    hideLoading()
                    Toast.makeText(context, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startTimer() {
        binding.btnResend.isEnabled = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = "Gửi lại sau $seconds giây"
                binding.btnResend.isEnabled = false
            }

            override fun onFinish() {
                binding.btnResend.isEnabled = true
                binding.tvTimer.text = "Gửi lại OTP"
            }
        }.start()
    }

    private fun showLoading(initialMessage: String = loadingMessages[0]) {
        binding.apply {
            progressBar.isVisible = true
            btnVerify.isEnabled = false
            btnResend.isEnabled = false

            tvLoadingMessage.apply {
                text = initialMessage
                isVisible = true
            }

            messageChangeHandler = Handler(Looper.getMainLooper())
            messageChangeHandler?.post(messageChangeRunnable)
        }
    }

    private fun hideLoading() {
        messageChangeHandler?.removeCallbacks(messageChangeRunnable)
        messageChangeHandler = null
        currentMessageIndex = 0

        binding.apply {
            progressBar.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    progressBar.isVisible = false
                    progressBar.alpha = 1f
                }

            tvLoadingMessage.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    tvLoadingMessage.isVisible = false
                    tvLoadingMessage.alpha = 1f
                    btnVerify.isEnabled = true
                    btnResend.isEnabled = true
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageChangeHandler?.removeCallbacks(messageChangeRunnable)
        messageChangeHandler = null
        countDownTimer?.cancel()
    }
}