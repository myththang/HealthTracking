package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.ProfileApi

class PersonalInfoRepository(
    private val api: ProfileApi
) : BaseRepository()