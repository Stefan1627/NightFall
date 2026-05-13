package com.nightfall.data.repo

import com.nightfall.core.result.Result
import com.nightfall.data.firebase.ChatDataSource
import com.nightfall.data.mappers.toDomain
import com.nightfall.data.mappers.toDto
import com.nightfall.domain.model.ChatMessage
import com.nightfall.domain.repo.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDataSource: ChatDataSource
) : ChatRepository {

    override suspend fun sendMessage(lobbyId: String, message: ChatMessage): Result<Unit> {
        return try {
            chatDataSource.sendMessage(lobbyId, message.toDto())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override fun observeMessages(lobbyId: String): Flow<List<ChatMessage>> {
        return chatDataSource.observeMessages(lobbyId).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }
}