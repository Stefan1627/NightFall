package com.nightfall.domain.model

data class GameState(
    val gameId: String = "",
    val lobbyId: String = "",
    val phase: GamePhase = GamePhase.LOBBY,
    val players: List<Player> = emptyList(),
    val votes: List<Vote> = emptyList(),
    val nightActions: List<NightAction> = emptyList(),
    val round: Int = 0,
    val winner: String? = null
)
