package com.nightfall.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nightfall.data.model.GameStateDto
import com.nightfall.data.model.NightActionDto
import com.nightfall.data.model.VoteDto
import com.nightfall.util.FirebasePaths
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GameDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {

    suspend fun initGameState(gameState: GameStateDto) {
        val gameRef = firebaseDatabase.getReference(FirebasePaths.game(gameState.lobbyId))
        suspendCancellableCoroutine<Unit> { continuation ->
            gameRef.setValue(gameState)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun observeGameState(lobbyId: String): Flow<GameStateDto?> = callbackFlow {
        val gameRef = firebaseDatabase.getReference(FirebasePaths.game(lobbyId))

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gameState = snapshot.getValue(GameStateDto::class.java)
                trySend(gameState)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        gameRef.addValueEventListener(listener)
        awaitClose { gameRef.removeEventListener(listener) }
    }

    suspend fun updatePhase(lobbyId: String, phase: String) {
        val phaseRef = firebaseDatabase.getReference("${FirebasePaths.game(lobbyId)}/currentPhase")
        suspendCancellableCoroutine<Unit> { continuation ->
            phaseRef.setValue(phase)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun updateRound(lobbyId: String, round: Int) {
        val roundRef = firebaseDatabase.getReference("${FirebasePaths.game(lobbyId)}/round")
        suspendCancellableCoroutine<Unit> { continuation ->
            roundRef.setValue(round)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun setWinner(lobbyId: String, winner: String) {
        val winnerRef = firebaseDatabase.getReference("${FirebasePaths.game(lobbyId)}/winner")
        suspendCancellableCoroutine<Unit> { continuation ->
            winnerRef.setValue(winner)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun setEliminatedPlayer(lobbyId: String, playerId: String?) {
        val ref = firebaseDatabase.getReference("${FirebasePaths.game(lobbyId)}/eliminatedPlayerId")
        suspendCancellableCoroutine<Unit> { continuation ->
            ref.setValue(playerId)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun submitVote(gameId: String, vote: VoteDto) {
        val voteRef = firebaseDatabase.getReference(
            FirebasePaths.vote(gameId, vote.voteId)
        )
        suspendCancellableCoroutine<Unit> { continuation ->
            voteRef.setValue(vote)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun observeVotes(gameId: String): Flow<List<VoteDto>> = callbackFlow {
        val votesRef = firebaseDatabase.getReference(FirebasePaths.votes(gameId))

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val votes = snapshot.children.mapNotNull { child ->
                    child.getValue(VoteDto::class.java)
                }
                trySend(votes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        votesRef.addValueEventListener(listener)
        awaitClose { votesRef.removeEventListener(listener) }
    }

    suspend fun submitNightAction(gameId: String, action: NightActionDto) {
        val actionRef = firebaseDatabase.getReference(
            FirebasePaths.nightAction(gameId, action.actorId)
        )
        suspendCancellableCoroutine<Unit> { continuation ->
            actionRef.setValue(action)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun observeNightActions(gameId: String): Flow<List<NightActionDto>> = callbackFlow {
        val actionsRef = firebaseDatabase.getReference(FirebasePaths.nightActions(gameId))

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val actions = snapshot.children.mapNotNull { child ->
                    child.getValue(NightActionDto::class.java)
                }
                trySend(actions)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        actionsRef.addValueEventListener(listener)
        awaitClose { actionsRef.removeEventListener(listener) }
    }

    suspend fun clearNightActions(gameId: String) {
        val actionsRef = firebaseDatabase.getReference(FirebasePaths.nightActions(gameId))
        suspendCancellableCoroutine<Unit> { continuation ->
            actionsRef.removeValue()
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun clearVotes(gameId: String) {
        val votesRef = firebaseDatabase.getReference(FirebasePaths.votes(gameId))
        suspendCancellableCoroutine<Unit> { continuation ->
            votesRef.removeValue()
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun updatePlayerAlive(lobbyId: String, playerId: String, isAlive: Boolean) {
        val aliveRef = firebaseDatabase.getReference(
            "${FirebasePaths.lobbyPlayer(lobbyId, playerId)}/isAlive"
        )
        suspendCancellableCoroutine<Unit> { continuation ->
            aliveRef.setValue(isAlive)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun updatePlayerRole(lobbyId: String, playerId: String, role: String) {
        val roleRef = firebaseDatabase.getReference(
            "${FirebasePaths.lobbyPlayer(lobbyId, playerId)}/role"
        )
        suspendCancellableCoroutine<Unit> { continuation ->
            roleRef.setValue(role)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}