package com.nightfall.domain.usecase.lobby

import com.nightfall.core.result.Result
import com.nightfall.domain.repo.AuthRepository
import com.nightfall.domain.repo.LobbyRepository
import javax.inject.Inject

class LeaveLobbyUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(lobbyId: String): Result<Unit> {
        val user = authRepository.getCurrentUser()
            ?: return Result.Error(IllegalStateException("User not authenticated"))
        return lobbyRepository.leaveLobby(lobbyId, user.uid)
    }
}
