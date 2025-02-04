package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.model.Menu
import com.fpt.edu.healthtracking.data.model.MenuDetail
import com.fpt.edu.healthtracking.data.request.AddMenuPlanDayRequest
import com.fpt.edu.healthtracking.data.request.AddMenuPlanMealRequest
import com.fpt.edu.healthtracking.data.request.AddMenuToMealRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MenuApi {
    @GET("MealPlanMember/get-all-meal-plans")
    suspend fun getMenus(
        @Header("Authorization") token: String
    ): List<Menu>

    @GET("MealPlanMember/search-meal-plan-for-member")
    suspend fun searchMenus(
        @Query("mealPlanName") query: String,
        @Header("Authorization") token: String
    ): List<Menu>

    @GET("MealPlanMember/get-meal-plan-detail-for-member")
    suspend fun getMenuDetail(
        @Query("mealPlanId") mealPlanId: Int,
        @Query("day") day: Int,
        @Header("Authorization") token: String
    ): MenuDetail

    @POST("MealPlanMember/add-meal-plan-to-diary-again")
    suspend fun addMenuPlanToWeek(
        @Query("mealPlanId") mealPlanId: Int,
        @Query("selectDate") selectDate: String,
        @Header("Authorization") token: String
    )

    @POST("MealPlanMember/add-meal-plan-detail-with-day-to-food-diary")
    suspend fun addMenuPlanDay(
        @Body request: AddMenuPlanDayRequest,
        @Header("Authorization") token: String
    )

    @POST("MealPlanMember/add-meal-plan-detail-with-meal-type-day-to-food-diary")
    suspend fun addMenuPlanMeal(
        @Body request: AddMenuPlanMealRequest,
        @Header("Authorization") token: String
    )
}