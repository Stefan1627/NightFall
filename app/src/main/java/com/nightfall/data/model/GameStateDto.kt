package com.nightfall.data.model

data class GameStateDto(
    val lobbyId: String = "",
    val currentPhase: String = "lobby",
    val round: Int = 0,
    val winner: String? = null,
    val eliminatedPlayerId: String? = null
)