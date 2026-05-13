package com.nightfall.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightfall.core.result.Result
import com.nightfall.core.session.SessionManager
import com.nightfall.domain.model.ChatMessage
import com.nightfall.domain.model.GamePhase
import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.NightAction
import com.nightfall.domain.model.Player
import com.nightfall.domain.model.Vote
import com.nightfall.domain.repo.GameRepository
import com.nightfall.domain.repo.LobbyRepository
import com.nightfall.domain.usecase.chat.ObserveMessageUseCase
import com.nightfall.domain.usecase.chat.SendMessageUseCase
import com.nightfall.domain.usecase.game.CheckWinConditionUseCase
import com.nightfall.domain.usecase.game.ObserveGameStateUseCase
import com.nightfall.domain.usecase.game.SubmitNightActionUseCase
import com.nightfall.domain.usecase.game.SubmitVoteUseCase
import com.nightfall.domain.usecase.game.TransitionPhaseUseCase
import com.nightfall.engine.GameEvent
import com.nightfall.engine.GamePhaseSerializer
import com.nightfall.engine.GameStateMachine
import com.nightfall.engine.PhaseManager
import com.nightfall.engine.RoleDistributor
import com.nightfall.engine.VoteManager
import com.nightfall.engine.WinConditionChecker
import com.nightfall.engine.GameOutcome
import com.nightfall.roles.RoleDefinition
import com.nightfall.roles.RoleRegistry
import com.nightfall.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val gameStateMachine: GameStateMachine,
    private val phaseManager: PhaseManager,
    private val voteManager: VoteManager,
    private val winConditionChecker: WinConditionChecker,
    private val roleDistributor: RoleDistributor,
    private val observeGameStateUseCase: ObserveGameStateUseCase,
    private val submitVoteUseCase: SubmitVoteUseCase,
    private val submitNightActionUseCase: SubmitNightActionUseCase,
    private val transitionPhaseUseCase: TransitionPhaseUseCase,
    private val checkWinConditionUseCase: CheckWinConditionUseCase,
    private val observeMessageUseCase: ObserveMessageUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val gameRepository: GameRepository,
    private val lobbyRepository: LobbyRepository
) : ViewModel() {

    private val lobbyId: String = savedStateHandle.get<String>("lobbyId") ?: ""

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _currentPhase = MutableStateFlow<GamePhase>(GamePhase.Lobby)
    val currentPhase: StateFlow<GamePhase> = _currentPhase.asStateFlow()

    private val _phaseRemainingMs = MutableStateFlow(0L)
    val phaseRemainingMs: StateFlow<Long> = _phaseRemainingMs.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _myRole = MutableStateFlow<RoleDefinition?>(null)
    val myRole: StateFlow<RoleDefinition?> = _myRole.asStateFlow()

    private val _votes = MutableStateFlow<List<Vote>>(emptyList())
    val votes: StateFlow<List<Vote>> = _votes.asStateFlow()

    private val _eliminatedPlayerId = MutableStateFlow<String?>(null)
    val eliminatedPlayerId: StateFlow<String?> = _eliminatedPlayerId.asStateFlow()

    private val _hasVoted = MutableStateFlow(false)
    val hasVoted: StateFlow<Boolean> = _hasVoted.asStateFlow()

    private val _hasSubmittedNightAction = MutableStateFlow(false)
    val hasSubmittedNightAction: StateFlow<Boolean> = _hasSubmittedNightAction.asStateFlow()

    private val _roleRevealShown = MutableStateFlow(false)
    val roleRevealShown: StateFlow<Boolean> = _roleRevealShown.asStateFlow()

    private val _hostId = MutableStateFlow("")
    val hostId: StateFlow<String> = _hostId.asStateFlow()

    private val _gameMode = MutableStateFlow<String?>(null)

    private var timerJob: Job? = null

    val currentUserId: String
        get() = sessionManager.getCurrentUserId() ?: ""

    val isHost: Boolean
        get() = currentUserId == _hostId.value

    init {
        if (lobbyId.isNotEmpty()) {
            startObserving()
        }
    }

    private fun startObserving() {
        // Observe game state
        viewModelScope.launch {
            observeGameStateUseCase(lobbyId).collect { state ->
                if (state != null) {
                    _gameState.value = state
                    val phase = GamePhaseSerializer.deserialize(state.currentPhase)
                    val previousPhase = _currentPhase.value
                    _currentPhase.value = phase

                    if (previousPhase != phase) {
                        onPhaseChanged(phase)
                    }
                }
            }
        }

        // Observe players
        viewModelScope.launch {
            lobbyRepository.observePlayers(lobbyId).collect { playerList ->
                _players.value = playerList

                // Update my role
                val myPlayer = playerList.find { it.playerId == currentUserId }
                if (myPlayer?.role != null) {
                    _myRole.value = RoleRegistry.getRole(myPlayer.role!!)
                }
            }
        }

        // Observe lobby for host info
        viewModelScope.launch {
            lobbyRepository.observeLobby(lobbyId).collect { lobby ->
                if (lobby != null) {
                    _hostId.value = lobby.hostId
                    _gameMode.value = lobby.gameMode
                }
            }
        }

        // Observe chat messages
        viewModelScope.launch {
            observeMessageUseCase(lobbyId).collect { messages ->
                _chatMessages.value = messages
            }
        }

        // Observe votes
        viewModelScope.launch {
            gameRepository.observeVotes(lobbyId).collect { voteList ->
                _votes.value = voteList
            }
        }
    }

    private fun onPhaseChanged(phase: GamePhase) {
        timerJob?.cancel()
        _hasVoted.value = false
        _hasSubmittedNightAction.value = false

        val durationMs = when (phase) {
            is GamePhase.Night -> Constants.NIGHT_PHASE_DURATION_MS
            is GamePhase.Day -> Constants.DAY_PHASE_DURATION_MS
            is GamePhase.Voting -> Constants.VOTING_PHASE_DURATION_MS
            else -> 0L
        }

        if (durationMs > 0) {
            timerJob = phaseManager.startTimer(
                scope = viewModelScope,
                durationMs = durationMs,
                onTick = { remaining -> _phaseRemainingMs.value = remaining },
                onExpire = {
                    if (isHost) {
                        advancePhase()
                    }
                }
            )
        }
    }

    fun submitVote(targetId: String) {
        if (_hasVoted.value) return
        viewModelScope.launch {
            val vote = Vote(
                voteId = UUID.randomUUID().toString(),
                gameId = lobbyId,
                voterId = currentUserId,
                targetId = targetId
            )
            when (submitVoteUseCase(lobbyId, vote, _gameState.value.currentPhase, _votes.value)) {
                is Result.Success -> _hasVoted.value = true
                is Result.Error -> { /* Handle error */ }
                Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun submitNightAction(targetId: String) {
        if (_hasSubmittedNightAction.value) return
        val role = _myRole.value ?: return
        viewModelScope.launch {
            val action = NightAction(
                actionId = UUID.randomUUID().toString(),
                gameId = lobbyId,
                actorId = currentUserId,
                targetId = targetId,
                abilityType = role.roleId
            )
            when (submitNightActionUseCase(lobbyId, action, _gameState.value.currentPhase)) {
                is Result.Success -> _hasSubmittedNightAction.value = true
                is Result.Error -> { /* Handle error */ }
                Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val message = ChatMessage(
                messageId = UUID.randomUUID().toString(),
                lobbyId = lobbyId,
                senderId = currentUserId,
                senderName = _players.value.find { it.playerId == currentUserId }?.displayName ?: "Unknown",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            sendMessageUseCase(lobbyId, message)
        }
    }

    fun advancePhase() {
        if (!isHost) return
        viewModelScope.launch {
            val current = _currentPhase.value
            val event = when (current) {
                is GamePhase.Night -> GameEvent.NightActionsComplete
                is GamePhase.Day -> GameEvent.DayTimerExpired
                is GamePhase.Voting -> {
                    // Process vote results
                    processVoteResults()
                    GameEvent.VotingComplete
                }
                is GamePhase.Elimination -> {
                    GameEvent.EliminationProcessed
                }
                is GamePhase.CheckWin -> {
                    val winner = checkWinConditionUseCase(_players.value)
                    if (winner != null) {
                        gameRepository.setWinner(lobbyId, winner)
                        GameEvent.WinnerFound
                    } else {
                        gameRepository.updateRound(lobbyId, _gameState.value.round + 1)
                        GameEvent.NoWinnerFound
                    }
                }
                is GamePhase.Lobby -> {
                    // Assign roles when starting the game
                    val roleMap = roleDistributor.distribute(
                        _players.value,
                        _gameMode.value ?: Constants.GAME_MODE_CLASSIC
                    )
                    
                    // Update all players in Firebase with their new roles
                    roleMap.forEach { (playerId, roleId) ->
                        lobbyRepository.updatePlayerRole(lobbyId, playerId, roleId)
                    }
                    
                    GameEvent.StartGame
                }
                else -> return@launch
            }

            val nextPhase = gameStateMachine.transition(current, event)
            val phaseStr = GamePhaseSerializer.serialize(nextPhase)
            transitionPhaseUseCase(lobbyId, phaseStr, currentUserId, _hostId.value)

            // Clear votes/night actions on phase transitions
            if (current is GamePhase.Night) {
                processNightActions()
                gameRepository.clearNightActions(lobbyId)
            }
            if (nextPhase is GamePhase.Night) {
                gameRepository.clearVotes(lobbyId)
            }
        }
    }

    private suspend fun processVoteResults() {
        val eliminatedId = voteManager.tally(_votes.value)
        _eliminatedPlayerId.value = eliminatedId
        if (eliminatedId != null) {
            gameRepository.updatePlayerAlive(lobbyId, eliminatedId, false)
        }
    }

    private suspend fun processNightActions() {
        val actions = mutableListOf<NightAction>()
        // Collect current night actions from Firebase
        gameRepository.observeNightActions(lobbyId).collect { actionList ->
            actions.addAll(actionList)
            return@collect
        }

        // Sort by priority: Doctor first, Mafia second, Detective third
        val sorted = actions.sortedBy { action ->
            when (action.abilityType) {
                "doctor" -> 0
                "mafia" -> 1
                "detective" -> 2
                else -> 3
            }
        }

        val protectedPlayers = mutableSetOf<String>()

        for (action in sorted) {
            when (action.abilityType) {
                "doctor" -> {
                    protectedPlayers.add(action.targetId)
                }
                "mafia" -> {
                    if (action.targetId !in protectedPlayers) {
                        gameRepository.updatePlayerAlive(lobbyId, action.targetId, false)
                    }
                }
                "detective" -> {
                    // Investigation result — no state change needed
                    // The detective sees the result via the NightAction record
                }
            }
        }
    }

    fun dismissRoleReveal() {
        _roleRevealShown.value = true
    }

    fun getPhaseDurationMs(): Long {
        return when (_currentPhase.value) {
            is GamePhase.Night -> Constants.NIGHT_PHASE_DURATION_MS
            is GamePhase.Day -> Constants.DAY_PHASE_DURATION_MS
            is GamePhase.Voting -> Constants.VOTING_PHASE_DURATION_MS
            else -> 0L
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}