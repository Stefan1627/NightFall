package com.nightfall.domain.repo

import com.nightfall.core.result.Result
import com.nightfall.domain.model.Lobby
import com.nightfall.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface LobbyRepository {
    suspend fun createLobby(lobby: Lobby): Result<Unit>
    suspend fun getLobby(lobbyId: String): Result<Lobby>
    suspend fun joinLobby(lobbyId: String, player: Player): Result<Unit>
    suspend fun leaveLobby(lobbyId: String, userId: String): Result<Unit>
    suspend fun updateLobbyStatus(lobbyId: String, status: String): Result<Unit>
    fun observeLobby(lobbyId: String): Flow<Lobby?>
}
