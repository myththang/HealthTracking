data class ChatResponse(
    val messageChatId: Int,
    val staffId: Int?,
    val memberId: Int,
    val rateStar: Int?,
    val createAt: String,
    val status: Boolean,
    val messageChatDetails: List<ChatDetailResponse> = emptyList()
)

data class ChatDetailResponse(
    val messageChatDetailsId: Int,
    val senderType: Int,
    val messageContent: String,
    val sentAt: String
)
