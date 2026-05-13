package com.nightfall.domain.model

data class User(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
