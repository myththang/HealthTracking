package com.fpt.edu.healthtracking.data.repository

import ChatDetailResponse
import ChatResponse
import com.fpt.edu.healthtracking.adapters.ChatTrainerMessage
import com.fpt.edu.healthtracking.api.ChatApi
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.ui.chat.ChatHubClient
import kotlinx.coroutines.flow.first

class ChatRepository(
    private val api: ChatApi,
    private val userPreferences: UserPreferences,
    internal val chatHub: ChatHubClient
) : BaseRepository(){

    suspend fun getMyChats(): Resource<List<ChatResponse>> = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getMyChats(token)
    }

    suspend fun createChat(initialMessage: String) = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.createChat(ChatApi.CreateChatRequest(initialMessage), token)
    }

    suspend fun getChatDetails(chatId: Int): Resource<List<ChatDetailResponse>> = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.getChatDetails(chatId,token)
    }

    suspend fun endChat(): Resource<Unit> = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.endChat(token)
    }

    suspend fun sendMessage(chatId: Int, message: String): Resource<Unit> = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.sendMessage(SendMessageRequest(chatId,message),token)
    }

    suspend fun rateChat(chatId: Int, rating: Int): Resource<Unit> = safeApiCall {
        val authState = userPreferences.authStateFlow.first()
        val token = "Bearer ${authState.accessToken}"
        api.rateChat(RatingRequest(chatId, rating), token)
    }
}

data class SendMessageRequest(
    val chatId: Int,
    val messageContent: String
)

data class RatingRequest(
    val chatId: Int,
    val ratingStar: Int
)