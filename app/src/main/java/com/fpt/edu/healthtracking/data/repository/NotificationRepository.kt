package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.NotificationApi
import com.fpt.edu.healthtracking.data.UserPreferences

class NotificationRepository(
    private val api:NotificationApi,
    private val userPreferences: UserPreferences
):BaseRepository(

)