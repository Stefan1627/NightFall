package com.nightfall.domain.model

data class GameState(
    val lobbyId: String = "",
    val currentPhase: String = "lobby",
    val round: Int = 0,
    val winner: String? = null,
    val eliminatedPlayerId: String? = null
)