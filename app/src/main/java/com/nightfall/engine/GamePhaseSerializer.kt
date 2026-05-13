package com.nightfall.engine

import com.nightfall.domain.model.GamePhase

object GamePhaseSerializer {

    fun serialize(phase: GamePhase): String {
        return when (phase) {
            is GamePhase.Lobby -> "lobby"
            is GamePhase.Night -> "night"
            is GamePhase.Day -> "day"
            is GamePhase.Voting -> "voting"
            is GamePhase.Elimination -> "elimination"
            is GamePhase.CheckWin -> "check_win"
            is GamePhase.EndGame -> "end_game"
        }
    }

    fun deserialize(raw: String): GamePhase {
        return when (raw) {
            "lobby" -> GamePhase.Lobby
            "night" -> GamePhase.Night
            "day" -> GamePhase.Day
            "voting" -> GamePhase.Voting
            "elimination" -> GamePhase.Elimination
            "check_win" -> GamePhase.CheckWin
            "end_game" -> GamePhase.EndGame
            else -> GamePhase.Lobby
        }
    }
}
