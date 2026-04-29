package com.nightfall.data.model

data class PlayerDto(
    val playerId: String = "",
    val displayName: String = "",
    val isAlive: Boolean = true,
    val isConnected: Boolean = false,
    val role: String? = null
)