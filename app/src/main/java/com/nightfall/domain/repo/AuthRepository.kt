package com.nightfall.domain.repo

import com.nightfall.core.result.Result
import com.nightfall.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun logout()
    fun observeAuthState(): Flow<User?>
    fun getCurrentUser(): User?
}
