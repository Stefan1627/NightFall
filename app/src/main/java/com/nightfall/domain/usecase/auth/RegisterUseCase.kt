package com.nightfall.domain.usecase.auth

import com.nightfall.core.result.Result
import com.nightfall.domain.model.User
import com.nightfall.domain.repo.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        if (email.isBlank() || password.isBlank() || displayName.isBlank())
            return Result.Error(IllegalArgumentException("All fields are required"))
        if (password.length < 6)
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters"))
        return repository.register(email, password, displayName)
    }
}
