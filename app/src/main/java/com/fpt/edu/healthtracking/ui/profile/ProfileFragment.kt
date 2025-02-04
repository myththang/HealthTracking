package com.fpt.edu.healthtracking.ui.profile

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.ProfileApi
import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.databinding.FragmentAccountDetailBinding
import com.fpt.edu.healthtracking.databinding.FragmentProfileBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.fpt.edu.healthtracking.util.DateUtils
import com.fpt.edu.healthtracking.util.file.ImageUtils
import kotlinx.coroutines.launch

class ProfileFragment :
    BaseFragment<AccountDetailViewModel, FragmentProfileBinding, AccountRepository>() {
    override fun getViewModel() = AccountDetailViewModel::class.java


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): AccountRepository {
        val api = remoteDataSource.buildApi(ProfileApi::class.java)
        return AccountRepository(
            api = api,
            preferences = userPreferences
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        loadMemberDataApi()

        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val profileImagePath = sharedPreferences?.getString("profile_image_path", null)

        Glide.with(this) // Or use context if outside a fragment
            .load(profileImagePath)
            .placeholder(R.drawable.default_avatar)
            .into(binding.ivProfileImage)

        //binding.ivProfileImage.setImageURI(ImageUtils.setImage(requireContext()))
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

    private fun observeViewModel() {
        // Observe the profile data
        viewModel.profileData.observe(viewLifecycleOwner) { profileData ->
            if (profileData != null) {
                updateProfileViews(profileData)
            } else {
                Log.d("AccountDetailFragment", "ProfileData is null")
            }
        }
        // Observe any errors
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                // Show the error message, you can use a Snackbar, Toast, or any other UI component
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfileViews(data: ProfileData) {
        data.imageMember.let { imageUrl ->
            // change image
        }
        val (formattedDob, age) = DateUtils.reformatSqlDateAndCalculateAge(data.dob)
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", MODE_PRIVATE)

        binding.tvUsername.text = sharedPreferences.getString("fullname","")
        binding.tvWeight.text = "Cân nặng \n" + data.weight.toString() + " kg"
        binding.tvHeight.text = "Chiều cao \n" + data.height.toString() + " cm"
        binding.tvAge.text = "Tuổi \n" + age
        binding.tvWeight.text = "Cân nặng \n" + data.weight.toString() + " kg"
        binding.tvHeight.text = "Chiều cao \n" + data.height.toString() + " cm"
        binding.tvAge.text = "Tuổi \n" + age
    }

    private fun setupClickListeners() {
        val navController: NavController = Navigation.findNavController(requireView())
        binding.ivBackButton.setOnClickListener {
            navController.navigateUp()
        }

        binding.btnAccount.setOnClickListener {
            navController.navigate(R.id.action_navigation_profile_to_navigation_account_detail)
        }

        binding.btnChangePassword.setOnClickListener {
            // Navigate to Change Password
            navController.navigate(R.id.action_navigation_profile_to_navigation_change_password)
        }


    }

}
