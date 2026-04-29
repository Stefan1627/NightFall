package com.nightfall.domain.usecase.lobby

import com.nightfall.core.result.Result
import com.nightfall.domain.repo.AuthRepository
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.util.Constants
import javax.inject.Inject

class StartGameUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(lobbyId: String): Result<Unit> {
        val user = authRepository.getCurrentUser()
            ?: return Result.Error(IllegalStateException("User not authenticated"))

        val lobby = when (val result = lobbyRepository.getLobby(lobbyId)) {
            is Result.Success -> result.data
            is Result.Error -> return result
            Result.Loading -> return Result.Loading
        }

        if (lobby.hostId != user.uid)
            return Result.Error(IllegalStateException("Only the host can start the game"))
        if (lobby.players.size < Constants.MIN_PLAYERS)
            return Result.Error(
                IllegalStateException("Need at least ${Constants.MIN_PLAYERS} players to start")
            )

        return lobbyRepository.updateLobbyStatus(lobbyId, "in_progress")
    }
}
