package com.nightfall.domain.usecase.game

import com.nightfall.domain.model.GameState
import com.nightfall.domain.repo.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGameStateUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(lobbyId: String): Flow<GameState?> {
        return gameRepository.observeGameState(lobbyId)
    }
}