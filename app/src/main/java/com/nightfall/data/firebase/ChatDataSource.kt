package com.nightfall.data.firebase

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nightfall.data.model.ChatMessageDto
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
class ChatDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {

    suspend fun sendMessage(lobbyId: String, message: ChatMessageDto) {
        val chatsRef = firebaseDatabase.getReference(FirebasePaths.chats(lobbyId))
        val newMsgRef = chatsRef.push()
        val messageToSave = message.copy(messageId = newMsgRef.key ?: message.messageId)
        suspendCancellableCoroutine<Unit> { continuation ->
            newMsgRef.setValue(messageToSave)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun observeMessages(lobbyId: String): Flow<List<ChatMessageDto>> = callbackFlow {
        val messages = mutableListOf<ChatMessageDto>()
        val chatsRef = firebaseDatabase.getReference(FirebasePaths.chats(lobbyId))

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessageDto::class.java)
                if (message != null) {
                    messages.add(message)
                    trySend(messages.toList())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Chat messages are immutable, no-op
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val message = snapshot.getValue(ChatMessageDto::class.java)
                if (message != null) {
                    messages.removeAll { it.messageId == message.messageId }
                    trySend(messages.toList())
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // no-op
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        chatsRef.addChildEventListener(listener)

        awaitClose {
            chatsRef.removeEventListener(listener)
        }
    }
}