package com.nightfall.core.session

import com.google.firebase.auth.FirebaseAuth
import com.nightfall.domain.model.User
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class SessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun isSessionActive(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun restoreSession(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return try {
            suspendCancellableCoroutine<User?> { continuation ->
                currentUser.getIdToken(false)
                    .addOnSuccessListener {
                        val user = User(
                            userId = currentUser.uid,
                            displayName = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            createdAt = currentUser.metadata?.creationTimestamp
                                ?: System.currentTimeMillis()
                        )
                        continuation.resume(user)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}