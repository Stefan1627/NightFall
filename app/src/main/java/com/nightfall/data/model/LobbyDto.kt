package com.nightfall.data.model

data class LobbyDto(
    val lobbyId: String = "",
    val hostId: String = "",
    val gameMode: String = "",
    val status: String = "",
    val players: Map<String, PlayerDto> = emptyMap()
)