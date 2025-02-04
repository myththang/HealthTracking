package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.model.AssignPlanRequest
import com.fpt.edu.healthtracking.data.model.ExercisePlan
import com.fpt.edu.healthtracking.data.model.ExercisePlanDetail
import com.fpt.edu.healthtracking.data.request.AddPlanToScheduleRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExercisePlanApi {
    @GET("ExercisePlan/GetAll")
    suspend fun getAllPlans(
        @Header("Authorization") token: String
    ): List<ExercisePlan>

    @GET("ExercisePlan/Get/{planId}")
    suspend fun getPlanDetail(
        @Path("planId") planId: Int,
        @Header("Authorization") token: String
    ): ExercisePlanDetail

    @GET("ExercisePlan/Search")
    suspend fun searchPlans(
        @Query("name") query: String,
        @Header("Authorization") token: String
    ): List<ExercisePlan>

    @POST("ExecriseDiaryDetail/AssignPlan")
    suspend fun assignPlan(
        @Body request: AssignPlanRequest,
        @Header("Authorization") token: String
    )
}