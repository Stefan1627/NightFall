package com.nightfall.domain.repo

import com.nightfall.core.result.Result
import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.NightAction
import com.nightfall.domain.model.Vote
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun initGameState(gameState: GameState): Result<Unit>
    fun observeGameState(lobbyId: String): Flow<GameState?>
    suspend fun updatePhase(lobbyId: String, phase: String): Result<Unit>
    suspend fun updateRound(lobbyId: String, round: Int): Result<Unit>
    suspend fun setWinner(lobbyId: String, winner: String): Result<Unit>
    suspend fun setEliminatedPlayer(lobbyId: String, playerId: String?): Result<Unit>
    suspend fun submitVote(gameId: String, vote: Vote): Result<Unit>
    fun observeVotes(gameId: String): Flow<List<Vote>>
    suspend fun submitNightAction(gameId: String, action: NightAction): Result<Unit>
    fun observeNightActions(gameId: String): Flow<List<NightAction>>
    suspend fun clearNightActions(gameId: String): Result<Unit>
    suspend fun clearVotes(gameId: String): Result<Unit>
    suspend fun updatePlayerAlive(lobbyId: String, playerId: String, isAlive: Boolean): Result<Unit>
    suspend fun updatePlayerRole(lobbyId: String, playerId: String, role: String): Result<Unit>
}