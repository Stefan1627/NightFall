package com.nightfall.data.mappers

import com.nightfall.data.model.LobbyDto
import com.nightfall.data.model.PlayerDto
import com.nightfall.domain.model.Lobby
import com.nightfall.domain.model.Player

fun PlayerDto.toDomain(): Player {
    return Player(
        playerId = playerId,
        displayName = displayName,
        isAlive = isAlive,
        isConnected = isConnected,
        role = role
    )
}

fun Player.toDto(): PlayerDto {
    return PlayerDto(
        playerId = playerId,
        displayName = displayName,
        isAlive = isAlive,
        isConnected = isConnected,
        role = role
    )
}

fun LobbyDto.toDomain(): Lobby {
    return Lobby(
        lobbyId = lobbyId,
        hostId = hostId,
        gameMode = gameMode,
        status = status,
        players = players.mapValues { (_, playerDto) ->
            playerDto.toDomain()
        }
    )
}

fun Lobby.toDto(): LobbyDto {
    return LobbyDto(
        lobbyId = lobbyId,
        hostId = hostId,
        gameMode = gameMode,
        status = status,
        players = players.mapValues { (_, player) ->
            player.toDto()
        }
    )
}