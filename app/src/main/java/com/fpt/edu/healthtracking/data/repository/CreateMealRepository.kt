package com.fpt.edu.healthtracking.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.fpt.edu.healthtracking.api.FoodApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.request.CreateMealRequest
import kotlinx.coroutines.flow.first
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CreateMealRepository(
    private val api: FoodApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    suspend fun createMeal(request: CreateMealRequest): Resource<Int> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.createMealPlan(token, request)
        }
    }

    suspend fun uploadMealImage(mealId: Int, imageUri: Uri, context: Context): Resource<Unit> {
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"

            // Convert Uri to File
            val file = File(getRealPathFromUri(context, imageUri) ?: throw Exception("Cannot get file path"))
            
            // Create request body
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
            val mealIdPart = mealId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            api.uploadMealImage(token, imagePart, mealIdPart)
        }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }
        return filePath
    }
}