package com.nightfall.data.mappers

import com.nightfall.data.model.UserDto
import com.nightfall.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        userId = userId,
        displayName = displayName,
        email = email,
        createdAt = createdAt
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        userId = userId,
        displayName = displayName,
        email = email,
        createdAt = createdAt
    )
}