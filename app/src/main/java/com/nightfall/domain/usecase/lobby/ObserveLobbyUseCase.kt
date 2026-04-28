package com.nightfall.domain.usecase.lobby

import com.nightfall.domain.model.Lobby
import com.nightfall.domain.repo.LobbyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLobbyUseCase @Inject constructor(private val lobbyRepository: LobbyRepository) {
    operator fun invoke(lobbyId: String): Flow<Lobby?> =
        lobbyRepository.observeLobby(lobbyId)
}
