package com.fpt.edu.healthtracking.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.API_KEY
    )
    private val chat = generativeModel.startChat()

    fun sendWelcomeMessage() {
        addMessage(ChatMessage(
            "Xin chào! Tôi là trợ lý AI. Tôi có thể giúp bạn trả lời các câu hỏi về dinh dưỡng, sức khỏe và lối sống lành mạnh. Hãy đặt câu hỏi cho tôi nhé!",
            false
        ))
    }

    fun sendMessage(message: String) {
        addMessage(ChatMessage(message, true))

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = chat.sendMessage(message)
                _isLoading.value = false

                val botMessage = ChatMessage(
                    content = response.text ?: "Xin lỗi, tôi không thể xử lý yêu cầu của bạn.",
                    isUser = false
                )
                addMessage(botMessage)

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }
}
