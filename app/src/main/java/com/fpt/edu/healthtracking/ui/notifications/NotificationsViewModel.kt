package com.fpt.edu.healthtracking.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.data.repository.AccountRepository
import com.fpt.edu.healthtracking.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationsViewModel  (private var repository: NotificationRepository
) : ViewModel(){
    private val _profileData = MutableLiveData<ProfileData?>()
    val profileData: LiveData<ProfileData?> = _profileData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error



}