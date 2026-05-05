package com.nightfall.data.repo

import com.google.firebase.database.FirebaseDatabase
import com.nightfall.core.result.Result
import com.nightfall.data.firebase.LobbyDataSource
import com.nightfall.data.mappers.toDomain
import com.nightfall.data.mappers.toDto
import com.nightfall.domain.model.Lobby
import com.nightfall.domain.model.Player
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.util.FirebasePaths
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LobbyRepositoryImpl @Inject constructor(
    private val lobbyDataSource: LobbyDataSource,
    private val firebaseDatabase: FirebaseDatabase
) : LobbyRepository {

    override suspend fun createLobby(lobby: Lobby): Result<String> {
        return try {
            val lobbyId = lobbyDataSource.createLobby(lobby.toDto())
            Result.Success(lobbyId)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun joinLobby(lobbyId: String, player: Player): Result<Unit> {
        return try {
            lobbyDataSource.joinLobby(lobbyId, player.toDto())



            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun leaveLobby(lobbyId: String, playerId: String): Result<Unit> {
        return try {
            lobbyDataSource.leaveLobby(lobbyId, playerId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override fun observeLobby(lobbyId: String): Flow<Lobby?> {
        return lobbyDataSource.observeLobby(lobbyId).map { dto ->
            dto?.toDomain()
        }
    }

    override fun observePlayers(lobbyId: String): Flow<List<Player>> {
        return lobbyDataSource.observePlayers(lobbyId).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun setPlayerConnected(
        lobbyId: String,
        playerId: String,
        connected: Boolean
    ): Result<Unit> {
        return try {
            lobbyDataSource.setPlayerConnected(lobbyId, playerId, connected)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun migrateHost(lobbyId: String, newHostId: String): Result<Unit> {
        return try {
            lobbyDataSource.migrateHost(lobbyId, newHostId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }
}