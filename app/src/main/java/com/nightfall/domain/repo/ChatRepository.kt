package com.nightfall.domain.repo

import com.nightfall.core.result.Result
import com.nightfall.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(lobbyId: String, message: ChatMessage): Result<Unit>
    fun observeMessages(lobbyId: String): Flow<List<ChatMessage>>
}