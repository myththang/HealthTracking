package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.GoalApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.GraphData
import com.fpt.edu.healthtracking.data.model.GoalRequestDTO
import com.fpt.edu.healthtracking.data.model.GoalResponseDTO
import kotlinx.coroutines.flow.first

class GoalRepository(
    private val api:GoalApi,
    private val userPreferences: UserPreferences
): BaseRepository() {

    suspend fun insertGoal(goalRequestDTO: GoalRequestDTO): Resource<Any> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.updateGoal(token, goalRequestDTO)
        }
    }
    suspend fun getGoalDetail(): Resource<GoalResponseDTO> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getGoalDetail(token)
        }
    }

    suspend fun getWeightGraphDetail(): Resource<GraphData> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getWeightForGraph(token)
        }
    }

}