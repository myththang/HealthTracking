package com.fpt.edu.healthtracking.api

import com.fpt.edu.healthtracking.data.request.AddExerciseRequest
import com.fpt.edu.healthtracking.data.request.AddMealRequest
import com.fpt.edu.healthtracking.data.request.UpdateExerciseRequest
import com.fpt.edu.healthtracking.data.responses.CardioResponse
import com.fpt.edu.healthtracking.data.responses.ExerciseDiaryResponse
import com.fpt.edu.healthtracking.data.responses.ExerciseListResponseItem
import com.fpt.edu.healthtracking.data.responses.OtherResponse
import com.fpt.edu.healthtracking.data.responses.ResistanceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApi {

    @GET("ExerciseDiary/member/exercise_diary_by_date")
    suspend fun getExerciseDiaryByDate(
        @Query("date") date: String,
        @Header("Authorization") token: String
    ): ExerciseDiaryResponse

    @GET("Exercise/Get-all-exercises-for-member")
    suspend fun getExerciseList(
        @Header("Authorization") token: String,
        @Query("search") search: String?,
        @Query("isCardioFilter") isCardioFilter: Int?
    ): List<ExerciseListResponseItem>

    @GET("Exercise/Get-exercise-cardio-detail-for-member/{ExerciseId}")
    suspend fun getCardioDetail(
        @Header("Authorization") token: String,
        @Path("ExerciseId") exerciseId: Int
    ): CardioResponse

    @GET("Exercise/Get-exercise-resistance-detail-for-member/{ExerciseId}")
    suspend fun getResistance(
        @Header("Authorization") token: String,
        @Path("ExerciseId") exerciseId: Int
    ): ResistanceResponse

    @GET("Exercise/Get-exercise-other-detail-for-member/{ExerciseId}")
    suspend fun getOther(
        @Header("Authorization") token: String,
        @Path("ExerciseId") exerciseId: Int
    ): OtherResponse

    @POST("ExecriseDiaryDetail/AddExercise")
    suspend fun addExerciseToDiary(
        @Header("Authorization") token: String,
        @Body request: AddExerciseRequest
    )

    @PUT("ExerciseDiary/update_is_practice")
    suspend fun updateState(
        @Header("Authorization") token: String,
        @Body request: UpdateExerciseRequest
    )

}