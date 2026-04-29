package com.nightfall.domain.usecase.lobby

import com.nightfall.core.result.Result
import com.nightfall.domain.model.Player
import com.nightfall.domain.repo.AuthRepository
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.util.Constants
import javax.inject.Inject

class JoinLobbyUseCase @Inject constructor(
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

        if (lobby.status != "waiting")
            return Result.Error(IllegalStateException("Game already started"))
        if (lobby.players.size >= Constants.MAX_PLAYERS)
            return Result.Error(IllegalStateException("Lobby is full"))

        val player = Player(userId = user.uid, displayName = user.displayName)
        return lobbyRepository.joinLobby(lobbyId, player)
    }
}
