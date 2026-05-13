package com.nightfall.domain.usecase.game

import com.nightfall.core.result.Result
import com.nightfall.domain.model.NightAction
import com.nightfall.domain.repo.GameRepository
import javax.inject.Inject

class SubmitNightActionUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        gameId: String,
        action: NightAction,
        currentPhase: String
    ): Result<Unit> {
        if (currentPhase != "night") {
            return Result.Error(IllegalStateException("Night phase is not active"))
        }
        if (action.actorId == action.targetId) {
            return Result.Error(IllegalStateException("You cannot target yourself"))
        }
        return gameRepository.submitNightAction(gameId, action)
    }
}