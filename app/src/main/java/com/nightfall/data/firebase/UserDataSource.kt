package com.nightfall.data.firebase

import com.google.firebase.database.FirebaseDatabase
import com.nightfall.data.model.UserDto
import com.nightfall.util.FirebasePaths
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class UserDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {

    suspend fun createUserProfile(user: UserDto) {
        val userRef = firebaseDatabase.getReference(FirebasePaths.user(user.userId))
        suspendCancellableCoroutine<Unit> { continuation ->
            userRef.setValue(user)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun getUserProfile(userId: String): UserDto? {
        return suspendCancellableCoroutine { continuation ->
            firebaseDatabase.getReference(FirebasePaths.user(userId))
                .get()
                .addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(UserDto::class.java)
                    continuation.resume(user)
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}
