package com.fpt.edu.healthtracking.ui.profile


import ProfileValidator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.StartActivity
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.DashboardApi
import com.fpt.edu.healthtracking.api.ProfileApi
import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.api.RemoteDataSource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.ProfileDataRequest

import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.data.repository.DashBoardRepository
import com.fpt.edu.healthtracking.databinding.FragmentAccountDetailBinding
import com.fpt.edu.healthtracking.databinding.FragmentHomeBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.ui.home.HomeViewModel
import com.fpt.edu.healthtracking.util.DateUtils
import com.fpt.edu.healthtracking.util.file.ImageUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AccountDetailFragment :
    BaseFragment<AccountDetailViewModel, FragmentAccountDetailBinding, AccountRepository>() {

    private var gender: Boolean =true
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.setSelectedImage(it)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        //binding.ivProfileImage.setImageURI(ImageUtils.setImage(requireContext()))
        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val profileImagePath = sharedPreferences?.getString("profile_image_path", null)

        Glide.with(this) // Or use context if outside a fragment
            .load(profileImagePath)
            .placeholder(R.drawable.default_avatar)
            .into(binding.ivProfileImage)
        observeViewModel()
        loadMemberDataApi()

        binding.etDob.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val str = s.toString().replace("/", "")
                if (str.length <= 8) {
                    var formatted = ""

                    // Add first slash after DD
                    if (str.length >= 2) {
                        formatted = str.substring(0, 2) + "/"
                        // Add second slash after MM
                        if (str.length >= 4) {
                            formatted += str.substring(2, 4) + "/"
                            if (str.length >= 5) {
                                formatted += str.substring(4)
                            }
                        } else if (str.length > 2) {
                            formatted += str.substring(2)
                        }
                    } else {
                        formatted = str
                    }

                    // Only update if the format is different to avoid infinite loop
                    if (formatted != s.toString()) {
                        binding.etDob.setText(formatted)
                        binding.etDob.setSelection(formatted.length)
                    }

                    // Validate date when complete
                    if (str.length == 8) {
                        validateAndShowError(formatted)
                    } else {
                    }
                }
                isFormatting = false
            }
        })

        // Set input type to number and max length
        binding.etDob.apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(android.text.InputFilter.LengthFilter(10))
            hint = "DD/MM/YYYY"
        }

        // Set click listener for back button
        binding.ivBackButton.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigateUp()
        }
        binding.etDob.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val str = s.toString().replace("/", "")
                if (str.length <= 8) {
                    var formatted = ""

                    // Add first slash after DD
                    if (str.length >= 2) {
                        formatted = str.substring(0, 2) + "/"
                        // Add second slash after MM
                        if (str.length >= 4) {
                            formatted += str.substring(2, 4) + "/"
                            if (str.length >= 5) {
                                formatted += str.substring(4)
                            }
                        } else if (str.length > 2) {
                            formatted += str.substring(2)
                        }
                    } else {
                        formatted = str
                    }

                    // Only update if the format is different to avoid infinite loop
                    if (formatted != s.toString()) {
                        binding.etDob.setText(formatted)
                        binding.etDob.setSelection(formatted.length)
                    }

                    // Validate date when complete
                    if (str.length == 8) {
                        validateAndShowError(formatted)
                    }
                }
                isFormatting = false
            }
        })


        binding.btnUpdateProfile.setOnClickListener {
            val username = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phoneNumber = binding.etPhone.text.toString().trim()
            val memberImage = selectedImageUri?.toString() ?: "" // Handle null case for image URI
            val dob = binding.etDob.text.toString().trim()

            val height = binding.etHeight.text.toString().trim()
            val weight = binding.etWeight.text.toString().trim()

            // Initialize validator
            val validator = ProfileValidator()

            // Validate each field
            val isNameValid = validator.validateName(username)
            val isEmailValid = validator.validateEmail(email)
            val isPhoneValid = validator.validatePhone(phoneNumber)
            val dobError = validator.validateDob(dob)
            val isHeightValid = validator.validateHeight(height)
            val isWeightValid = validator.validateWeight(weight)

            // Check if all fields are valid
            if (isNameValid && isEmailValid && isPhoneValid && dobError == null && isHeightValid && isWeightValid) {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dob)
                val formattedDob = date.let { outputFormat.format(it) }
                val profileUpdateRequest = ProfileDataRequest(
                    username = username,
                    email = email,
                    phoneNumber = phoneNumber,
                    dob = formattedDob,
                    gender = this.gender,
                    height = height.toInt(),
                    weight = weight.toInt()
                )
                loadApi(profileUpdateRequest)
            } else {
                if (!isNameValid) binding.etName.error = "Vui lòng nhập tên hợp lệ"
                if (!isEmailValid) binding.etEmail.error = "Vui lòng nhập email hợp lệ"
                if (!isPhoneValid) binding.etPhone.error = "Số điện thoại không hợp lệ"
                if (dobError != null) binding.etDob.error = dobError
                if (!isHeightValid) binding.etHeight.error = "Chiều cao không hợp lệ"
                if (!isWeightValid) binding.etWeight.error = "Cân nặng không hợp lệ"
            }
        }

        binding.ivProfileImage.setOnClickListener {
            launchImagePicker()
        }
        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            updateImageView(uri)
        }

    }

    private fun updateImageView(uri: Uri?) {
        binding.apply {
            if (uri != null) {



                viewModel.uploadImage(uri,requireContext())
            } else {

            }
        }
    }
    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun validateAndShowError(dateStr: String) {
        val error = when {
            dateStr.isEmpty() -> {
                "Vui lòng nhập ngày sinh"
            }

            dateStr.length < 10 -> {
                "Vui lòng nhập đầy đủ ngày sinh"
            }

            else -> {
                try {
                    val parts = dateStr.split("/")
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()

                    when {
                        !DateUtils.isValidDate(day, month, year) -> {
                            "Ngày sinh không hợp lệ"
                        }

                        !DateUtils.isValidYear(year, month, day) -> {
                            "Tuổi phải từ 18 đến 100"
                        }

                        else -> null
                    }
                } catch (e: Exception) {
                    "Định dạng ngày sinh không hợp lệ"
                }
            }
        }

        if (error == null) {
            viewModel.setDob(dateStr)
        }

    }





    private fun observeViewModel() {

        // Observe the profile data
        viewModel.profileData.observe(viewLifecycleOwner) { profileData ->
            if (profileData != null) {
                updateProfileViews(profileData)
            } else {
            }
        }

        // Observe any errors
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                // Show the error message, you can use a Snackbar, Toast, or any other UI component
                Log.d("profileEror",errorMsg)
            }
        }
        viewModel.success.observe(viewLifecycleOwner) { successMsg ->
            successMsg?.let {
                viewModel.loadAccountData()
                Glide.with(requireContext())
                    .load(viewModel.selectedImageUri.value)
                    .centerCrop()
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error_food)
                    .into(binding.ivProfileImage)
                // Show the error message, you can use a Snackbar, Toast, or any other UI component
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private lateinit var profileData:ProfileData
    private fun updateProfileViews(it: ProfileData) {
        profileData=it
        it.imageMember.let { imageUrl ->
            // change image
        }
        val (formattedDob, age) = DateUtils.reformatSqlDateAndCalculateAge(it.dob)

        gender=profileData.gender
        binding.tvWeight.text = "Cân nặng \n" + it.weight.toString() + " kg"
        binding.tvHeight.text = "Chiều cao \n" + it.height.toString() + " cm"
        binding.tvAge.text = "Tuổi \n" + age

        // Update the EditText fields with the profile data
        binding.etName.setText(it.username)
        binding.etEmail.setText(it.email)
        binding.etPhone.setText(it.phoneNumber)
        binding.etDob.setText(formattedDob)
        binding.etHeight.setText(it.height.toString())  // Assuming height is Double or Int
        binding.etWeight.setText(it.weight.toString())  // Assuming weight is Double or Int
    }


    private fun loadMemberDataApi() {
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                authState.accessToken?.let { token ->
                    viewModel.loadAccountData()
                }
            }
        }
    }

    private fun loadApi(profileData: ProfileDataRequest) {
        lifecycleScope.launch {
            userPreferences.authStateFlow.collect { authState ->
                authState.accessToken?.let { token ->
                    viewModel.updateProfileData(profileData)
                    if (profileData.weight != (profileData as? ProfileData)?.weight) {
                        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("last_weight_change_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                            Date()
                        ))
                        editor.apply()
                    }
                }
            }
        }
    }

    override fun getViewModel() = AccountDetailViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAccountDetailBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): AccountRepository {
        val api = remoteDataSource.buildApi(ProfileApi::class.java)
        return AccountRepository(
            api = api,
            preferences = userPreferences
        )
    }



    private fun setProfilePic(context: Context, imageUri: Uri, imageView: CircleImageView) {
        Glide.with(context)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }
}
