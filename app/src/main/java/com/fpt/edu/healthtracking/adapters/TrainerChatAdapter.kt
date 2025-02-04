package com.fpt.edu.healthtracking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import java.text.SimpleDateFormat
import java.util.Locale

class TrainerChatAdapter : RecyclerView.Adapter<TrainerChatAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<ChatTrainerMessage>()

    fun submitList(newMessages: List<ChatTrainerMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatTrainerMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = when (viewType) {
            VIEW_TYPE_MEMBER -> R.layout.item_message_user
            else -> R.layout.item_message_bot
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromTrainer) VIEW_TYPE_TRAINER else VIEW_TYPE_MEMBER
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        //private val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        //private val senderName: TextView? = itemView.findViewById(R.id.tvSenderName)

        fun bind(message: ChatTrainerMessage) {
            messageText.text = message.content
           // timestamp.text = formatTimestamp(message.timestamp)
            //senderName?.text = if (message.isFromTrainer) "Trainer" else "You"
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = sdf.parse(timestamp)
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
            } catch (e: Exception) {
                timestamp
            }
        }
    }

    companion object {
        const val VIEW_TYPE_MEMBER = 1
        const val VIEW_TYPE_TRAINER = 2
    }
}

data class ChatTrainerMessage(
    val id: Int,
    val content: String,
    val timestamp: String,
    val isFromTrainer: Boolean
)