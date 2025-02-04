package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.model.DailyCaloriesDto
import com.fpt.edu.healthtracking.data.model.DailyNutritionDto
import com.fpt.edu.healthtracking.data.model.MacroNutrientDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NutritionApi {

    @GET("Calories/daily-calories")
    suspend fun getDailyCalories(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): DailyCaloriesDto

    @GET("Nutrients/daily-nutrition")
    suspend fun getDailyNutrition(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): DailyNutritionDto

    @GET("Macro/daily-macros")
    suspend fun getDailyMacros(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): MacroNutrientDto
}