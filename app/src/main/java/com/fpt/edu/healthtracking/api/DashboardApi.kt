package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.model.DashboardData
import com.fpt.edu.healthtracking.data.responses.ExerciseStreakResponse
import com.fpt.edu.healthtracking.data.responses.FoodStreakResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DashboardApi {
    @GET("MainDashBoardMobile/Get-main-dashboard-for-member-by-id")
    suspend fun getMainDashboardForMemberById(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): DashboardData

    @GET("FoodDiary/get-streak")
    suspend fun getFoodStreak(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): FoodStreakResponse

    @GET("ExerciseDiary/member/exercise_diary_streak")
    suspend fun getExerciseStreak(
        @Header("Authorization") token: String,
        @Query("SelectDate") date: String
    ): ExerciseStreakResponse
}

