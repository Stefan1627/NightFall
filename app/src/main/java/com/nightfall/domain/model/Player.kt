package com.nightfall.domain.model

data class Player(
    val userId: String,
    val displayName: String,
    val isAlive: Boolean = true,
    val role: Role? = null
)
