package com.fpt.edu.healthtracking.data.model

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody

data class ProfileData(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("imageMember") val imageMember: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("gender") val gender: Boolean,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int
)
data class ProfileDataRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("gender") val gender: Boolean,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int
)

data class UploadResponse(
    val message: String,
    val image: Image
)

data class Image(
    val id: Int,
    val publicId: String,
    val url: String,
    val originalFileName: String,
    val fileSize: Int,
    val uploadDate: String
)
data class ImageUploadDto(
    val file: MultipartBody.Part,    // This will hold the file part
    val description: RequestBody?    // This will hold the description part (optional)
)