package com.nightfall.domain.model

data class Player(
    val playerId: String = "",
    val displayName: String = "",
    val isAlive: Boolean = true,
    val isConnected: Boolean = false,
    val role: String? = null,
    val isProtected: Boolean = false
)
