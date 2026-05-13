package com.nightfall.data.repo

import com.nightfall.core.result.Result
import com.nightfall.data.firebase.AuthDataSource
import com.nightfall.domain.model.User
import com.nightfall.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signIn(email, password)
            Result.Success(firebaseUser.toDomainUser())
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            authDataSource.register(email, password)
            authDataSource.updateDisplayName(displayName)
            val firebaseUser = authDataSource.getCurrentUser()
                ?: throw IllegalStateException("User not found after registration")
            Result.Success(firebaseUser.toDomainUser())
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun logout() {
        authDataSource.signOut()
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.toDomainUser()
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override fun getCurrentUser(): User? {
        return authDataSource.getCurrentUser()?.toDomainUser()
    }

    private fun FirebaseUser.toDomainUser(): User {
        return User(
            userId = uid,
            displayName = displayName ?: "",
            email = email ?: "",
            createdAt = metadata?.creationTimestamp ?: System.currentTimeMillis()
        )
    }
}