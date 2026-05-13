package com.nightfall.domain.usecase.lobby

import com.nightfall.core.result.Result
import com.nightfall.domain.repo.AuthRepository
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.util.Constants
import javax.inject.Inject

import com.nightfall.domain.model.GameState

class StartGameUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
    private val gameRepository: com.nightfall.domain.repo.GameRepository,
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

        if (lobby.hostId != user.userId)
            return Result.Error(IllegalStateException("Only the host can start the game"))
        if (lobby.players.size < Constants.MIN_PLAYERS)
            return Result.Error(
                IllegalStateException("Need at least ${Constants.MIN_PLAYERS} players to start")
            )

        val gameState = GameState(
            lobbyId = lobbyId,
            currentPhase = "lobby",
            round = 1
        )
        val initResult = gameRepository.initGameState(gameState)
        if (initResult is Result.Error) {
            return initResult
        }

        return lobbyRepository.updateLobbyStatus(lobbyId, "in_progress")
    }
}
