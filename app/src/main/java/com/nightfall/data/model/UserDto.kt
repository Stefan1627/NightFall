package com.nightfall.data.model

data class UserDto(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Long = 0L
)