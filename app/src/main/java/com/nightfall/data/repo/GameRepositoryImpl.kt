package com.nightfall.data.repo

import com.nightfall.core.result.Result
import com.nightfall.data.firebase.GameDataSource
import com.nightfall.data.mappers.toDomain
import com.nightfall.data.mappers.toDto
import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.NightAction
import com.nightfall.domain.model.Vote
import com.nightfall.domain.repo.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gameDataSource: GameDataSource
) : GameRepository {

    override suspend fun initGameState(gameState: GameState): Result<Unit> {
        return try {
            gameDataSource.initGameState(gameState.toDto())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override fun observeGameState(lobbyId: String): Flow<GameState?> {
        return gameDataSource.observeGameState(lobbyId).map { dto ->
            dto?.toDomain()
        }
    }

    override suspend fun updatePhase(lobbyId: String, phase: String): Result<Unit> {
        return try {
            gameDataSource.updatePhase(lobbyId, phase)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updateRound(lobbyId: String, round: Int): Result<Unit> {
        return try {
            gameDataSource.updateRound(lobbyId, round)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun setWinner(lobbyId: String, winner: String): Result<Unit> {
        return try {
            gameDataSource.setWinner(lobbyId, winner)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun submitVote(gameId: String, vote: Vote): Result<Unit> {
        return try {
            gameDataSource.submitVote(gameId, vote.toDto())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override fun observeVotes(gameId: String): Flow<List<Vote>> {
        return gameDataSource.observeVotes(gameId).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun submitNightAction(gameId: String, action: NightAction): Result<Unit> {
        return try {
            gameDataSource.submitNightAction(gameId, action.toDto())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override fun observeNightActions(gameId: String): Flow<List<NightAction>> {
        return gameDataSource.observeNightActions(gameId).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun clearNightActions(gameId: String): Result<Unit> {
        return try {
            gameDataSource.clearNightActions(gameId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun clearVotes(gameId: String): Result<Unit> {
        return try {
            gameDataSource.clearVotes(gameId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updatePlayerAlive(
        lobbyId: String,
        playerId: String,
        isAlive: Boolean
    ): Result<Unit> {
        return try {
            gameDataSource.updatePlayerAlive(lobbyId, playerId, isAlive)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updatePlayerRole(
        lobbyId: String,
        playerId: String,
        role: String
    ): Result<Unit> {
        return try {
            gameDataSource.updatePlayerRole(lobbyId, playerId, role)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }
}