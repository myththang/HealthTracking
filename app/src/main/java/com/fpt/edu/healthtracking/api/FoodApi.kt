package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.MemberMeal
import com.fpt.edu.healthtracking.data.request.AddFoodToDiaryRequest
import com.fpt.edu.healthtracking.data.request.AddMealRequest
import com.fpt.edu.healthtracking.data.request.CreateMealRequest
import com.fpt.edu.healthtracking.data.responses.FoodDetailResponse
import com.fpt.edu.healthtracking.data.responses.FoodDiaryResponse
import com.fpt.edu.healthtracking.data.responses.FoodStreakResponse
import com.fpt.edu.healthtracking.data.responses.MealDetailResponse
import com.fpt.edu.healthtracking.data.responses.PreviousMealResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface FoodApi {
    @GET("FoodDiary/Get-Food-dairy-detail")
    suspend fun getFoodDiary(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): FoodDiaryResponse

    @GET("Food/get-all-foods-for-member")
    suspend fun getAllFoods(
        @Header("Authorization") token: String
    ): List<Food>

    @GET("Food/search-foods-for-member")
    suspend fun searchFoods(
        @Query("foodName") query: String,
        @Header("Authorization") token: String
    ): List<Food>

    @POST("FoodDiary/addFoodListToDiary")
    suspend fun addFoodToDiary(
        @Header("Authorization") token: String,
        @Body request: AddFoodToDiaryRequest
    ): Unit

    @POST("MealMember/create-meal-for-member")
    suspend fun createMealPlan(
        @Header("Authorization") token: String,
        @Body request: CreateMealRequest
    ): Int

    @GET("MealMember/get-meal-member-detail/{id}")
    suspend fun getMealMembers(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): MealDetailResponse

    @GET("MealMember/get-all-meal-members")
    suspend fun getAllMealMembers(
        @Header("Authorization") token: String
    ): List<MemberMeal>

    @POST("MealMember/Add-meal-member-to-diary")
    suspend fun addMealToDiary(
        @Header("Authorization") token: String,
        @Body request: AddMealRequest
    ): String

    @GET("Food/get-food-for-member-by-id")
    suspend fun getFoodDetail(
        @Query("FoodId") FoodId: Int,
        @Query("SelectDate") SelectDate: String,
        @Header("Authorization") token: String
    ): FoodDetailResponse

    @Multipart
    @PUT("MealMember/upload-image-meal")
    suspend fun uploadMealImage(
        @Header("Authorization") token: String,
        @Part imageFile: MultipartBody.Part,
        @Part("mealMemberid") mealId: RequestBody
    )

    @DELETE("MealMember/delete-meal-member/{id}")
    suspend fun deleteMealMember(
        @Header("Authorization") token: String,
        @Path("id") mealId: Int
    )

    @DELETE("FoodDiary/deleteFoodListFromDiary")
    suspend fun deleteFoodFromDiary(
        @Query("foodDiaryDetailId") diaryDetailId: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("FoodDiary/getFoodHistory")
    suspend fun getFoodHistory(
        @Header("Authorization") token: String
    ): List<Food>

    @GET("FoodDiary/get-food-suggestion")
    suspend fun getFoodSuggestions(
        @Header("Authorization") token: String
    ): List<Food>

    @GET("FoodDiary/get-streak")
    suspend fun getFoodStreak(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): FoodStreakResponse

    @GET("MealMember/get-meal-before-by-meal-type")
    suspend fun getMealBeforeByMealType(
        @Header("Authorization") token: String,
        @Query("MealTypePrevious") mealType: Int
    ): Int

    @GET("MealMember/Copy-previous-meal")
    suspend fun getPreviousMeal(
        @Header("Authorization") token: String,
        @Query("dirayId") diaryId: Int,
        @Query("Mealtype") mealType: Int
    ): PreviousMealResponse

    @POST("MealMember/Insert-copy-previous-meal")
    suspend fun copyPreviousMeal(
        @Header("Authorization") token: String,
        @Body body: Map<String, Int>
    ): Response<Unit>
}

