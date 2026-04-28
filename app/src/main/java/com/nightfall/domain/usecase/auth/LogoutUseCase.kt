package com.nightfall.domain.usecase.auth

import com.nightfall.core.result.Result
import com.nightfall.domain.repo.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
