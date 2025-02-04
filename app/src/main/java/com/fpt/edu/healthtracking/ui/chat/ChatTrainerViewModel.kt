package com.fpt.edu.healthtracking.ui.chat

import ChatDetailResponse
import ChatResponse
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.edu.healthtracking.adapters.ChatTrainerMessage
import com.fpt.edu.healthtracking.api.Resource
import com.fpt.edu.healthtracking.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ChatTrainerViewModel(
    val repository: ChatRepository,
    //private val chatHub: ChatHubClient

) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatTrainerMessage>>()
    val messages: LiveData<List<ChatTrainerMessage>> = _messages

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _waitingForTrainer = MutableLiveData<Boolean>()
    val waitingForTrainer: LiveData<Boolean> = _waitingForTrainer

    private val _createChat = MutableLiveData<Boolean>()
    val createChat: LiveData<Boolean> = _createChat

    private val _showRating = MutableLiveData<Boolean>()
    val showRating: LiveData<Boolean> = _showRating

    private var currentChatId: Int? = null
    private var chatUpdateJob: Job? = null

    sealed class ChatState {
        object NO_CHAT : ChatState()
        object WAITING_FOR_TRAINER : ChatState()
        object ACTIVE_CHAT : ChatState()
    }

    private val _chatState = MutableLiveData<ChatState>()
    val chatState: LiveData<ChatState> = _chatState

    init {
        setupSignalR()
    }

    private fun setupSignalR() {
        Log.d("ChatTrainerViewModel", "Setting up SignalR connection...")
        repository.chatHub.onReceiveMessage { senderId, message, timestamp ->
            Log.e("Hello","Hello")
            //Log.d("ChatTrainerViewModel", "Received message: $message from Sender: $senderId")
            viewModelScope.launch {
                loadChatMessages()
            }
        }
    }

    fun ChatDetailResponse.toChatMessage(): ChatTrainerMessage {
        return ChatTrainerMessage(
            id = this.messageChatDetailsId,
            content = this.messageContent,
            timestamp = this.sentAt,
            isFromTrainer = this.senderType != 1
        )
    }

    fun checkExistingChat() {
        viewModelScope.launch {
            try {
                _loading.value = true
                when (val result = repository.getMyChats()) {
                    is Resource.Success -> {
                        if (result.value.isEmpty()) {
                            _chatState.value = ChatState.NO_CHAT
                        } else {
                            val chat = result.value.first()
                            if (chat.staffId == null) {
                                _chatState.value = ChatState.WAITING_FOR_TRAINER
                                startWaitingCheck()
                            } else {
                                currentChatId = chat.messageChatId
                                _chatState.value = ChatState.ACTIVE_CHAT
                                repository.chatHub.connect()
                                repository.chatHub.joinChat(chat.messageChatId.toString())
                                loadInitialMessages(chat.messageChatId)
                                startChatUpdates()
                            }
                        }
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể kết nối với server"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    fun createNewChat(init: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                when (val result = repository.createChat(init)) {
                    is Resource.Success -> checkExistingChat()
                    is Resource.Failure -> _error.value = "Không thể tạo cuộc trò chuyện mới"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun handleExistingChat(chat: ChatResponse) {
        currentChatId = chat.messageChatId
        viewModelScope.launch {
            try {
                if (chat.staffId == null) {
                    _chatState.value = ChatState.WAITING_FOR_TRAINER
                    startWaitingCheck()
                } else {
                    _chatState.value = ChatState.ACTIVE_CHAT
                    setupActiveChatConnection(chat.messageChatId)
                }
            } catch (e: Exception) {
                _error.value = "Không thể tham gia cuộc trò chuyện: ${e.message}"
            }
        }
    }
    private fun setupActiveChatConnection(chatId: Int) {
        viewModelScope.launch {
            try {
                repository.chatHub.connect()
                repository.chatHub.joinChat(chatId.toString())
                loadInitialMessages(chatId)
                startChatUpdates()
            } catch (e: Exception) {
                _error.value = "Không thể tham gia cuộc trò chuyện: ${e.message}"
            }
        }
    }


    private var waitingCheckJob: Job? = null

    private fun startWaitingCheck() {
        waitingCheckJob?.cancel()
        waitingCheckJob = viewModelScope.launch {
            while (true) {
                when (val result = repository.getMyChats()) {
                    is Resource.Success -> {
                        if (result.value.isNotEmpty() && result.value.first().staffId != null) {
                            handleExistingChat(result.value.first())
                            break
                        }
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể kiểm tra trạng thái trainer"
                    }
                }
                delay(5000)
            }
        }
    }

    private fun loadInitialMessages(chatId: Int) {
        viewModelScope.launch {
            try {
                when (val result = repository.getChatDetails(chatId)) {
                    is Resource.Success -> {
                        _messages.value = result.value.map { it.toChatMessage() }
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể tải tin nhắn"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }



    suspend fun loadChatMessages() {
        currentChatId?.let { chatId ->
            try {
                when (val result = repository.getChatDetails(chatId)) {
                    is Resource.Success -> {
                        _messages.value = result.value.map { it.toChatMessage() }
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể tải tin nhắn"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun sendMessage(content: String) {
        Log.e("e","aaaa")
        viewModelScope.launch {
            currentChatId?.let { chatId ->
                try {
                    _loading.value = true
                    when (val result = repository.sendMessage(chatId, content)) {
                        is Resource.Success -> {
                            loadChatMessages()
                        }
                        is Resource.Failure -> {
                            _error.value = "Không thể gửi tin nhắn"
                        }
                    }
                } catch (e: Exception) {
                    _error.value = "Lỗi: ${e.message}"
                } finally {
                    _loading.value = false
                }
            }
        }
    }

    fun rateChat(rating: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                currentChatId?.let { chatId ->
                    when (val result = repository.rateChat(chatId, rating)) {
                        is Resource.Success -> {
                            _showRating.value = false
                           // _createChat.value = true
                            checkExistingChat()
                        }
                        is Resource.Failure -> {
                            _error.value = "Không thể đánh giá cuộc trò chuyện"
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    private fun startChatUpdates() {
        chatUpdateJob?.cancel()
        chatUpdateJob = viewModelScope.launch {
            while (true) {
                loadChatMessages()
                delay(5000)
            }
        }
    }
    fun endChat() {
        viewModelScope.launch {
            try {
                _loading.value = true
                when (val result = repository.endChat()) {
                    is Resource.Success -> {
                        _showRating.value = true
                            chatUpdateJob?.cancel()
                    }
                    is Resource.Failure -> {
                        _error.value = "Không thể kết thúc cuộc trò chuyện"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentChatId?.let { chatId ->
            repository.chatHub.leaveChat(chatId.toString())
        }
        repository.run { chatHub.disconnect() }
        chatUpdateJob?.cancel()
        waitingCheckJob?.cancel()
    }
}





