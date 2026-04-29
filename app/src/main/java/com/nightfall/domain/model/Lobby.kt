package com.nightfall.domain.model

class Lobby {
    val lobbyId: String = "",
    val hostId: String = "",
    val gameMode: String = "",
    val status: String = "",
    val players: Map<String, Player> = emptyMap()
}