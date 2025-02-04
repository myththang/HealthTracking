package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.DashboardApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.DashboardData
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DashBoardRepository(
    private val api: DashboardApi,
    private val preferences: UserPreferences,
) : BaseRepository() {

    suspend fun getDashboardData(date: LocalDate): Resource<DashboardData> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            val formattedDate = date.toString()
            api.getMainDashboardForMemberById(token,formattedDate)
        }
    }
}
