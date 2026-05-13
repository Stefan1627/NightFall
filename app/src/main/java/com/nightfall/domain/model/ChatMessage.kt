package com.nightfall.domain.model

data class ChatMessage(
    val messageId: String = "",
    val lobbyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)