package com.fpt.edu.healthtracking.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R

class ChatMessageAdapter : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun removeLastMessage() {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            if (!lastMessage.isUser) {
                messages.removeAt(messages.size - 1)
                notifyItemRemoved(messages.size)
            }
        }
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_message_user
        } else {
            R.layout.item_message_bot
        }
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(message: ChatMessage) {
            messageText.text = message.content
        }
    }

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_BOT = 2
    }
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean
) 