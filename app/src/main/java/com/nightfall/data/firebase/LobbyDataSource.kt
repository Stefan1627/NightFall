package com.nightfall.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nightfall.data.model.LobbyDto
import com.nightfall.data.model.PlayerDto
import com.nightfall.util.FirebasePaths
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LobbyDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {

    suspend fun createLobby(lobby: LobbyDto): String {
        val lobbiesRef = firebaseDatabase.getReference(FirebasePaths.LOBBIES)
        val newLobbyRef = lobbiesRef.push()
        val lobbyId = newLobbyRef.key
            ?: throw IllegalStateException("Failed to generate lobby ID")

        val lobbyToSave = lobby.copy(lobbyId = lobbyId)
        newLobbyRef.setValueSuspend(lobbyToSave)

        return lobbyId
    }

    suspend fun joinLobby(lobbyId: String, player: PlayerDto) {
        val playerRef = firebaseDatabase.getReference(
            FirebasePaths.lobbyPlayer(lobbyId, player.playerId)
        )
        playerRef.setValueSuspend(player)
    }

    suspend fun leaveLobby(lobbyId: String, playerId: String) {
        val playerRef = firebaseDatabase.getReference(
            FirebasePaths.lobbyPlayer(lobbyId, playerId)
        )
        playerRef.removeValueSuspend()
    }

    fun observeLobby(lobbyId: String): Flow<LobbyDto?> = callbackFlow {
        val lobbyRef = firebaseDatabase.getReference(FirebasePaths.lobby(lobbyId))

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lobby = snapshot.getValue(LobbyDto::class.java)
                trySend(lobby)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        lobbyRef.addValueEventListener(listener)

        awaitClose {
            lobbyRef.removeEventListener(listener)
        }
    }

    fun observePlayers(lobbyId: String): Flow<List<PlayerDto>> = callbackFlow {
        val playersRef = firebaseDatabase.getReference(FirebasePaths.lobbyPlayers(lobbyId))

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull { child ->
                    child.getValue(PlayerDto::class.java)
                }
                trySend(players)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        playersRef.addValueEventListener(listener)

        awaitClose {
            playersRef.removeEventListener(listener)
        }
    }

    suspend fun setPlayerConnected(
        lobbyId: String,
        playerId: String,
        connected: Boolean
    ) {
        val connectedRef = firebaseDatabase.getReference(
            "${FirebasePaths.lobbyPlayer(lobbyId, playerId)}/isConnected"
        )
        connectedRef.setValueSuspend(connected)
    }

    suspend fun migrateHost(lobbyId: String, newHostId: String) {
        val hostRef = firebaseDatabase.getReference(
            "${FirebasePaths.lobby(lobbyId)}/hostId"
        )
        hostRef.setValueSuspend(newHostId)
    }

    private suspend fun DatabaseReference.setValueSuspend(value: Any?) {
        suspendCancellableCoroutine<Unit> { continuation ->
            setValue(value)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    private suspend fun DatabaseReference.removeValueSuspend() {
        suspendCancellableCoroutine<Unit> { continuation ->
            removeValue()
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}