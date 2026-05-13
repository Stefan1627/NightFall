package com.nightfall.data.model

data class ChatMessageDto(
    val messageId: String = "",
    val lobbyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)