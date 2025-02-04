package com.fpt.edu.healthtracking.api

import ChatDetailResponse
import ChatResponse
import com.fpt.edu.healthtracking.adapters.ChatTrainerMessage
import com.fpt.edu.healthtracking.data.repository.RatingRequest
import com.fpt.edu.healthtracking.data.repository.SendMessageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {
    @GET("member/MemberChat/my-chats")
    suspend fun getMyChats(@Header("Authorization") token: String): List<ChatResponse>

    @POST("member/MemberChat/create-chat")
    suspend fun createChat(
        @Body request: CreateChatRequest,
        @Header("Authorization") token: String
    )

    @GET("member/MemberChat/chat-details/{chatId}")
    suspend fun getChatDetails(@Path("chatId") chatId: Int,@Header("Authorization") token: String): List<ChatDetailResponse>

    @GET("member/MemberChat/end-chats")
    suspend fun endChat(@Header("Authorization") token: String)

    @POST("member/MemberChat/send-message")
    suspend fun sendMessage(
        @Body request: SendMessageRequest,
        @Header("Authorization") token: String
    )

    @POST("member/MemberChat/rate-chat")
    suspend fun rateChat(
        @Body request: RatingRequest,
        @Header("Authorization") token: String
    )

    data class CreateChatRequest(
        val initialMessage: String
    )
}
