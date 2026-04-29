package com.nightfall.domain.usecase.auth

import com.nightfall.domain.model.User
import com.nightfall.domain.repo.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): User? = repository.getCurrentUser()
}
