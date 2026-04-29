package com.nightfall.domain.repo

import com.nightfall.core.result.Result
import com.nightfall.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): User?
}
