package com.nightfall.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signIn(email: String, password: String): FirebaseUser {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("Sign in succeeded but user is null")
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun register(email: String, password: String): FirebaseUser {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("Registration succeeded but user is null")
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun updateDisplayName(name: String) {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        suspendCancellableCoroutine<Unit> { continuation ->
            user.updateProfile(profileUpdates)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun signOut() {
        firebaseAuth.signOut()
    }
}