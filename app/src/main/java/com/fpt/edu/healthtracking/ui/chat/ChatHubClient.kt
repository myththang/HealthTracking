package com.fpt.edu.healthtracking.ui.chat

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatHubClient(baseUrl: String) {
    private val hubConnection: HubConnection = HubConnectionBuilder
        .create("${baseUrl}chatHub")
        .build()

    suspend fun connect() {
        if (hubConnection.connectionState == HubConnectionState.DISCONNECTED) {
            try {
                withContext(Dispatchers.IO) {
                    hubConnection.start().blockingAwait()
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to connect to SignalR hub: ${e.message}")
            }
        }
    }

    fun disconnect() {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.stop()
        }
    }

    fun joinChat(chatId: String) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("JoinChat", chatId)
        } else {
            throw IllegalStateException("Cannot join chat. Connection is not active.")
        }
    }

    fun leaveChat(chatId: String) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("LeaveChat", chatId)
        }
    }

    fun onReceiveMessage(callback: (SenderId: String, Message: String, Timestamp: String) -> Unit) {
        hubConnection.on("ReceiveMessage", { response ->

        }, Any::class.java)
    }

    val isConnected: Boolean
        get() = hubConnection.connectionState == HubConnectionState.CONNECTED
}