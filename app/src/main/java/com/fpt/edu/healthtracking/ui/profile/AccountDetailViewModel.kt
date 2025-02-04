package com.fpt.edu.healthtracking.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.DashboardData
import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.data.model.ProfileDataRequest
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AccountDetailViewModel(
    private var repository: AccountRepository
) : ViewModel(){
    private val _profileData = MutableLiveData<ProfileData?>()
    val profileData: LiveData<ProfileData?> = _profileData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    private val _dob = MutableLiveData<String>()
    val dob: LiveData<String> = _dob

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setSelectedImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    // Function to load dashboard data
    fun loadAccountData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.getMemberProfile()) {
                    is Resource.Success -> {
                        _profileData.value = result.value
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to load dashboard data: ${result.errorBody?.string()}"
                        _profileData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _profileData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProfileData(profileData: ProfileDataRequest) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.updateProfile(profileData)) {
                    is Resource.Success<*> -> {
                        _success.value="Update Successfully"
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to update profile: ${result.errorBody?.string()}"
                        _profileData.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _profileData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun setDob(dateStr: String) {
        _dob.value = dateStr
    }

    fun uploadImage(uri: Uri,context : Context) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (val result = repository.uploadImage(uri,context)) {
                    is Resource.Success<*> -> {
                        // Handle successful upload (you can add more logic as needed)
                        _success.value= "Upload Successfully"
                        _error.value = null
                    }
                    is Resource.Failure -> {
                        _error.value = "Failed to upload image: ${result.errorBody?.string()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }



}