package com.fpt.edu.healthtracking.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.fpt.edu.healthtracking.api.ProfileApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.model.Food
import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.data.model.ProfileDataRequest
import com.fpt.edu.healthtracking.data.model.UploadResponse
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AccountRepository(
    private val api: ProfileApi,
    private val preferences: UserPreferences
):
BaseRepository()
{
    suspend fun updateProfile(profileData: ProfileDataRequest): Any{
    return safeApiCall {
        val authState = preferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.updateProfile(profileData,token)
    }
}
    suspend fun getMemberProfile(): Resource<ProfileData>{
        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.getMemberProfile(token)
        }
    }

    suspend fun uploadImage(imageUri: Uri, context: Context): Any {
        val file = File(getRealPathFromUri(context, imageUri) ?: throw Exception("Cannot get file path"))
        // Create request body
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)

        return safeApiCall {
            val authState = preferences.authStateFlow.first()
            val token = "Bearer ${authState.accessToken}"
            api.uploadImageAvatarMember(imagePart,token)
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