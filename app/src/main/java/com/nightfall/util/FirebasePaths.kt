package com.nightfall.util

object FirebasePaths {
    const val USERS = "users"
    const val LOBBIES = "lobbies"
    const val LOBBY_PLAYERS = "lobby_players"
    const val GAMES = "games"
    const val VOTES = "votes"
    const val NIGHT_ACTIONS = "night_actions"
    const val CHATS = "chats"

    fun user(userId: String): String = "$USERS/$userId"
    fun lobby(lobbyId: String): String = "$LOBBIES/$lobbyId"
    fun lobbyPlayers(lobbyId: String): String = "$LOBBY_PLAYERS/$lobbyId"
    fun lobbyPlayer(lobbyId: String, playerId: String): String = "$LOBBY_PLAYERS/$lobbyId/$playerId"
    fun game(lobbyId: String): String = "$GAMES/$lobbyId"
    fun votes(gameId: String): String = "$VOTES/$gameId"
    fun vote(gameId: String, voteId: String): String = "$VOTES/$gameId/$voteId"
    fun nightActions(gameId: String): String = "$NIGHT_ACTIONS/$gameId"
    fun nightAction(gameId: String, actorId: String): String = "$NIGHT_ACTIONS/$gameId/$actorId"
    fun chats(lobbyId: String): String = "$CHATS/$lobbyId"
    fun lobbyHost(lobbyId: String): String = "${lobby(lobbyId)}/hostId"
fun playerConnection(lobbyId: String, playerId: String): String =
    "${lobbyPlayer(lobbyId, playerId)}/isConnected"
}