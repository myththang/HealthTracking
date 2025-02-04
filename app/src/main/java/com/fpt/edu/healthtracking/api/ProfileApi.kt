package com.fpt.edu.healthtracking.api


import com.fpt.edu.healthtracking.data.model.ProfileData
import com.fpt.edu.healthtracking.data.model.ProfileDataRequest
import com.fpt.edu.healthtracking.data.model.UploadResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part


interface ProfileApi {


        @PUT("Users/editMemberProfile")
        suspend fun updateProfile(
            @Body request: ProfileDataRequest,
            @Header("Authorization") token: String
        )

    @GET("Users/viewMemberProfile")
    suspend fun getMemberProfile(
        @Header("Authorization") token: String
    ): ProfileData

    @Multipart
    @PUT("Users/upload-image-avatar-member")
    suspend fun uploadImageAvatarMember(
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    )

}

