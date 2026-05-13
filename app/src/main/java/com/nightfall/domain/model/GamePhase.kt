package com.nightfall.domain.model

sealed class GamePhase {
    object Lobby : GamePhase()
    object Night : GamePhase()
    object Day : GamePhase()
    object Voting : GamePhase()
    object Elimination : GamePhase()
    object CheckWin : GamePhase()
    object EndGame : GamePhase()
}