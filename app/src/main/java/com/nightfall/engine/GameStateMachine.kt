package com.nightfall.engine

import com.nightfall.domain.model.GamePhase
import javax.inject.Inject

/**
 * Game events that trigger state transitions.
 */
sealed class GameEvent {
    object StartGame : GameEvent()
    object NightActionsComplete : GameEvent()
    object DayTimerExpired : GameEvent()
    object VotingComplete : GameEvent()
    object EliminationProcessed : GameEvent()
    object WinnerFound : GameEvent()
    object NoWinnerFound : GameEvent()
}

/**
 * Pure state machine — no side effects, no Firebase calls.
 * Determines the next GamePhase given the current phase and an event.
 */
class GameStateMachine @Inject constructor() {

    fun transition(currentPhase: GamePhase, event: GameEvent): GamePhase {
        return when (currentPhase) {
            is GamePhase.Lobby -> when (event) {
                is GameEvent.StartGame -> GamePhase.Night
                else -> currentPhase
            }
            is GamePhase.Night -> when (event) {
                is GameEvent.NightActionsComplete -> GamePhase.Day
                else -> currentPhase
            }
            is GamePhase.Day -> when (event) {
                is GameEvent.DayTimerExpired -> GamePhase.Voting
                else -> currentPhase
            }
            is GamePhase.Voting -> when (event) {
                is GameEvent.VotingComplete -> GamePhase.Elimination
                else -> currentPhase
            }
            is GamePhase.Elimination -> when (event) {
                is GameEvent.EliminationProcessed -> GamePhase.CheckWin
                else -> currentPhase
            }
            is GamePhase.CheckWin -> when (event) {
                is GameEvent.WinnerFound -> GamePhase.EndGame
                is GameEvent.NoWinnerFound -> GamePhase.Night
                else -> currentPhase
            }
            is GamePhase.EndGame -> currentPhase // Terminal state
        }
    }
}