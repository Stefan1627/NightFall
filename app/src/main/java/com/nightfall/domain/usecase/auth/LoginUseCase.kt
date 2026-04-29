package com.nightfall.domain.usecase.auth

import com.nightfall.core.result.Result
import com.nightfall.domain.model.User
import com.nightfall.domain.repo.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank())
            return Result.Error(IllegalArgumentException("Email and password cannot be empty"))
        if (password.length < 6)
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters"))
        return repository.login(email, password)
    }
}
