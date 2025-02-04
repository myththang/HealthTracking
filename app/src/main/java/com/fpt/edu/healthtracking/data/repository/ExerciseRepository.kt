package com.fpt.edu.healthtracking.data.repository

import com.fpt.edu.healthtracking.api.ExerciseApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.request.AddExerciseRequest
import com.fpt.edu.healthtracking.data.request.UpdateExerciseRequest
import com.fpt.edu.healthtracking.data.responses.CardioResponse
import com.fpt.edu.healthtracking.data.responses.ExerciseListResponseItem
import com.fpt.edu.healthtracking.data.responses.ResistanceResponse
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import com.fpt.edu.healthtracking.data.responses.ExerciseDiaryResponse

class ExerciseRepository(
    private val api: ExerciseApi,
    private val userPreferences: UserPreferences
) : BaseRepository() {

    suspend fun getExerciseDiaryByDate(date: Date): Resource<ExerciseDiaryResponse> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(date)
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            
            try {
                val response = api.getExerciseDiaryByDate(formattedDate, token)
                Log.d("ExerciseRepository", "Raw Response: $response")
                
                if (response.exerciseDiaryDetails.isNullOrEmpty()) {
                    Log.d("ExerciseRepository", "Empty diary details")
                    Resource.Success(
                        ExerciseDiaryResponse(
                            exerciseDiaryId = response.exerciseDiaryId,
                            memberId = response.memberId,
                            exercisePlanId = response.exercisePlanId,
                            date = response.date,
                            totalDuration = response.totalDuration,
                            totalCaloriesBurned = response.totalCaloriesBurned,
                            exerciseDiaryDetails = emptyList()
                        )
                    )
                } else {
                    Log.d("ExerciseRepository", "Success with diary details")
                    Resource.Success(response)
                }
            } catch (e: Exception) {
                Log.e("ExerciseRepository", "API Error: ${e.message}", e)
                Resource.Failure(false, null, null)
            }
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Repository Error: ${e.message}", e)
            Resource.Failure(false, null, null)
        }
    }

    suspend fun getExerciseList(search: String, isCardioFilter: Int?): Resource<List<ExerciseListResponseItem>> {
        return safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getExerciseList(token, search, isCardioFilter )
        }
    }

    suspend fun getResistanceDetail(id:Int)= safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getResistance(token,id)

    }

    suspend fun getOtherDetail(id:Int)= safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getOther(token,id)

    }

    suspend fun getCardioDetail(id:Int) = safeApiCall {
            val authState = userPreferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getCardioDetail(token,id)
    }

    suspend fun addToDiary(diaryId:Int,exerciseId: Int,duration:Int,isPractice:Boolean,caloriesBurned:Int) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        val request = AddExerciseRequest(
            exerciseDiaryId = diaryId,
            exerciseId = exerciseId,
            durationInMinutes = duration,
            isPractice = isPractice,
            caloriesBurned = caloriesBurned
        )
        api.addExerciseToDiary(token,request)
    }

    suspend fun updateState(diaryId: Int, diaryDetailId: Int, practice: Boolean) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        val request = UpdateExerciseRequest(
            diaryId = diaryId,
            exerciseDiaryDetailId = diaryDetailId,
            isPractice = practice
        )
        api.updateState(token,request)
    }

}