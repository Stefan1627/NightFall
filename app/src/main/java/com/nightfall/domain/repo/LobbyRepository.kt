package com.nightfall.domain.repo

import com.nightfall.core.result.Result
import com.nightfall.domain.model.Lobby
import com.nightfall.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface LobbyRepository {
    suspend fun createLobby(lobby: Lobby): Result<String>
    suspend fun joinLobby(lobbyId: String, player: Player): Result<Unit>
    suspend fun leaveLobby(lobbyId: String, playerId: String): Result<Unit>
    fun observeLobby(lobbyId: String): Flow<Lobby?>
    fun observePlayers(lobbyId: String): Flow<List<Player>>
    suspend fun setPlayerConnected(lobbyId: String, playerId: String, connected: Boolean): Result<Unit>
    suspend fun migrateHost(lobbyId: String, newHostId: String): Result<Unit>
    suspend fun updateLobbyStatus(lobbyId: String, status: String): Result<Unit>
}
