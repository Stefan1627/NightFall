package com.nightfall.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightfall.core.result.Result
import com.nightfall.core.session.SessionManager
import com.nightfall.domain.model.Lobby
import com.nightfall.domain.model.Player
import com.nightfall.domain.usecase.lobby.CreateLobbyUseCase
import com.nightfall.domain.usecase.lobby.JoinLobbyUseCase
import com.nightfall.domain.usecase.lobby.LeaveLobbyUseCase
import com.nightfall.domain.usecase.lobby.ObserveLobbyUseCase
import com.nightfall.domain.repo.LobbyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LobbyUiState {
    object Idle : LobbyUiState()
    object Loading : LobbyUiState()
    data class Active(val lobby: Lobby) : LobbyUiState()
    data class Error(val message: String) : LobbyUiState()
}

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val createLobbyUseCase: CreateLobbyUseCase,
    private val joinLobbyUseCase: JoinLobbyUseCase,
    private val leaveLobbyUseCase: LeaveLobbyUseCase,
    private val observeLobbyUseCase: ObserveLobbyUseCase,
    private val startGameUseCase: com.nightfall.domain.usecase.lobby.StartGameUseCase,
    private val lobbyRepository: LobbyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _lobbyState = MutableStateFlow<LobbyUiState>(LobbyUiState.Idle)
    val lobbyState: StateFlow<LobbyUiState> = _lobbyState.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _currentLobbyId = MutableStateFlow<String?>(null)
    val currentLobbyId: StateFlow<String?> = _currentLobbyId.asStateFlow()

    private var observeLobbyJob: Job? = null
    private var observePlayersJob: Job? = null

    fun createLobby(gameMode: String) {
        viewModelScope.launch {
            _lobbyState.value = LobbyUiState.Loading
            when (val result = createLobbyUseCase(gameMode)) {
                is Result.Success -> {
                    val lobby = result.data
                    _currentLobbyId.value = lobby.lobbyId

                    // Auto-join the lobby as host BEFORE emitting Active state
                    // This prevents the ViewModel scope from being cancelled by navigation
                    joinLobbyUseCase(lobby.lobbyId)

                    _lobbyState.value = LobbyUiState.Active(lobby)
                    startObserving(lobby.lobbyId)
                }
                is Result.Error -> {
                    _lobbyState.value = LobbyUiState.Error(
                        result.message ?: result.exception.message ?: "Failed to create lobby"
                    )
                }
                Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun joinLobby(lobbyId: String) {
        viewModelScope.launch {
            _lobbyState.value = LobbyUiState.Loading
            when (val result = joinLobbyUseCase(lobbyId)) {
                is Result.Success -> {
                    _currentLobbyId.value = lobbyId
                    startObserving(lobbyId)
                }
                is Result.Error -> {
                    _lobbyState.value = LobbyUiState.Error(
                        result.message ?: result.exception.message ?: "Failed to join lobby"
                    )
                }
                Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun leaveLobby() {
        val lobbyId = _currentLobbyId.value ?: return
        viewModelScope.launch {
            leaveLobbyUseCase(lobbyId)
            stopObserving()
            _lobbyState.value = LobbyUiState.Idle
            _currentLobbyId.value = null
            _players.value = emptyList()
        }
    }

    fun startObserving(lobbyId: String) {
        stopObserving()
        _currentLobbyId.value = lobbyId

        observeLobbyJob = viewModelScope.launch {
            observeLobbyUseCase(lobbyId).collect { lobby ->
                if (lobby != null) {
                    _lobbyState.value = LobbyUiState.Active(lobby)
                }
            }
        }

        observePlayersJob = viewModelScope.launch {
            lobbyRepository.observePlayers(lobbyId).collect { playerList ->
                _players.value = playerList
            }
        }
    }

    private fun stopObserving() {
        observeLobbyJob?.cancel()
        observePlayersJob?.cancel()
    }

    fun startGame() {
        val lobbyId = _currentLobbyId.value ?: return
        viewModelScope.launch {
            startGameUseCase(lobbyId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopObserving()
    }
}