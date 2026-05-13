package com.nightfall.domain.usecase.game

import com.nightfall.core.result.Result
import com.nightfall.domain.repo.GameRepository
import javax.inject.Inject

class TransitionPhaseUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        lobbyId: String,
        newPhase: String,
        currentUserId: String,
        hostId: String
    ): Result<Unit> {
        if (currentUserId != hostId) {
            return Result.Error(IllegalStateException("Only the host can transition phases"))
        }
        return gameRepository.updatePhase(lobbyId, newPhase)
    }
}