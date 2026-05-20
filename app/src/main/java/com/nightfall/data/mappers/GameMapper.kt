package com.nightfall.data.mappers

import com.nightfall.data.model.ChatMessageDto
import com.nightfall.data.model.GameStateDto
import com.nightfall.data.model.NightActionDto
import com.nightfall.data.model.VoteDto
import com.nightfall.domain.model.ChatMessage
import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.NightAction
import com.nightfall.domain.model.Vote

// ChatMessage mappers
fun ChatMessageDto.toDomain(): ChatMessage {
    return ChatMessage(
        messageId = messageId,
        lobbyId = lobbyId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        timestamp = timestamp
    )
}

fun ChatMessage.toDto(): ChatMessageDto {
    return ChatMessageDto(
        messageId = messageId,
        lobbyId = lobbyId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        timestamp = timestamp
    )
}

// GameState mappers
fun GameStateDto.toDomain(): GameState {
    return GameState(
        lobbyId = lobbyId,
        currentPhase = currentPhase,
        round = round,
        winner = winner,
        eliminatedPlayerId = eliminatedPlayerId
    )
}

fun GameState.toDto(): GameStateDto {
    return GameStateDto(
        lobbyId = lobbyId,
        currentPhase = currentPhase,
        round = round,
        winner = winner,
        eliminatedPlayerId = eliminatedPlayerId
    )
}

// Vote mappers
fun VoteDto.toDomain(): Vote {
    return Vote(
        voteId = voteId,
        gameId = gameId,
        voterId = voterId,
        targetId = targetId
    )
}

fun Vote.toDto(): VoteDto {
    return VoteDto(
        voteId = voteId,
        gameId = gameId,
        voterId = voterId,
        targetId = targetId
    )
}

// NightAction mappers
fun NightActionDto.toDomain(): NightAction {
    return NightAction(
        actionId = actionId,
        gameId = gameId,
        actorId = actorId,
        targetId = targetId,
        abilityType = abilityType
    )
}

fun NightAction.toDto(): NightActionDto {
    return NightActionDto(
        actionId = actionId,
        gameId = gameId,
        actorId = actorId,
        targetId = targetId,
        abilityType = abilityType
    )
}
