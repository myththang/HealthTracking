package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.ExercisePlanApi
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.AssignPlanRequest
import kotlinx.coroutines.flow.first

class ExercisePlanRepository(
    private val api: ExercisePlanApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    suspend fun getAllPlans() = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getAllPlans(token)
    }

    suspend fun getPlanDetail(planId: Int) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getPlanDetail(planId, token)
    }

    suspend fun searchPlans(query: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.searchPlans(query, token)
    }

    suspend fun assignPlan(planId: Int, startDate: String) = safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        val request = AssignPlanRequest(planId, startDate)
        api.assignPlan(request, token)
    }
}