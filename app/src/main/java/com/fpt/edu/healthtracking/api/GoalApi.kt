package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.model.GraphData
import com.fpt.edu.healthtracking.data.model.GoalRequestDTO
import com.fpt.edu.healthtracking.data.model.GoalResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface GoalApi {
    @POST("Goal/add-goal")
    suspend fun updateGoal(
        @Header("Authorization") token: String,
        @Body goalRequestDTO: GoalRequestDTO
    ): Any // Use the appropriate response type

    @GET("Goal/get-goal-detail")
    suspend fun getGoalDetail(
        @Header("Authorization") token: String
    ): GoalResponseDTO

    @GET("Goal/get-info-goal-weight-member-for-graph")
    suspend fun getWeightForGraph(
        @Header("Authorization") token: String,
    ): GraphData
}