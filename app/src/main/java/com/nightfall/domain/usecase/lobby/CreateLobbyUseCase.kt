package com.nightfall.domain.usecase.lobby

import com.nightfall.core.result.Result
import com.nightfall.domain.model.Lobby
import com.nightfall.domain.repo.AuthRepository
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.util.Constants
import java.util.UUID
import javax.inject.Inject

class CreateLobbyUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(gameMode: String = Constants.GAME_MODE_CLASSIC): Result<Lobby> {
        val user = authRepository.getCurrentUser()
            ?: return Result.Error(IllegalStateException("User not authenticated"))
        val pin = (1000..9999).random().toString()
        val lobby = Lobby(
            lobbyId = pin,
            hostId = user.userId,
            gameMode = gameMode,
            status = "waiting"
        )
        return when (val result = lobbyRepository.createLobby(lobby)) {
            is Result.Success -> Result.Success(lobby.copy(lobbyId = result.data))
            is Result.Error -> result
            Result.Loading -> Result.Loading
        }
    }
}
