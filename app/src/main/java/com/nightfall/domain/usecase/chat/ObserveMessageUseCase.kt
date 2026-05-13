package com.nightfall.domain.usecase.chat

import com.nightfall.domain.model.ChatMessage
import com.nightfall.domain.repo.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(lobbyId: String): Flow<List<ChatMessage>> {
        return chatRepository.observeMessages(lobbyId)
    }
}