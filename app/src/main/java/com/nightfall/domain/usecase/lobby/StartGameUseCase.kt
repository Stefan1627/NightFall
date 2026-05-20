package com.nightfall.domain.usecase.lobby

import com.nightfall.core.result.Result
import com.nightfall.domain.model.GameState
import com.nightfall.domain.repo.AuthRepository
import com.nightfall.domain.repo.GameRepository
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.engine.RoleDistributor
import com.nightfall.util.Constants
import javax.inject.Inject

class StartGameUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    private val roleDistributor: RoleDistributor
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

        val players = when (val result = lobbyRepository.getPlayers(lobbyId)) {
            is Result.Success -> result.data
            is Result.Error -> return result
            Result.Loading -> return Result.Loading
        }

        if (players.size < Constants.MIN_PLAYERS)
            return Result.Error(
                IllegalStateException("Need at least ${Constants.MIN_PLAYERS} players to start")
            )

        // Distribute roles and write to Firebase
        val roleMap = roleDistributor.distribute(players, lobby.gameMode)
        roleMap.forEach { (playerId, roleId) ->
            val roleResult = lobbyRepository.updatePlayerRole(lobbyId, playerId, roleId)
            if (roleResult is Result.Error) return roleResult
        }

        // Initialize game state starting at night phase directly
        val gameState = GameState(
            lobbyId = lobbyId,
            currentPhase = "night",
            round = 1
        )
        val initResult = gameRepository.initGameState(gameState)
        if (initResult is Result.Error) return initResult

        return lobbyRepository.updateLobbyStatus(lobbyId, "in_progress")
    }
}
