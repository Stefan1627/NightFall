package com.nightfall.domain.usecase.chat

import com.nightfall.core.result.Result
import com.nightfall.domain.model.ChatMessage
import com.nightfall.domain.repo.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(lobbyId: String, message: ChatMessage): Result<Unit> {
        if (message.text.isBlank()) {
            return Result.Error(IllegalArgumentException("Message cannot be empty"))
        }
        if (message.text.length > 300) {
            return Result.Error(IllegalArgumentException("Message must be under 300 characters"))
        }
        return chatRepository.sendMessage(lobbyId, message)
    }
}