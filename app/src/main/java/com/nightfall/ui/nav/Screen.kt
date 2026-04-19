package com.nightfall.ui.nav

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreateLobby : Screen("create_lobby")
    object LobbyWaiting : Screen("lobby_waiting/{lobbyId}") {
        fun createRoute(lobbyId: String) = "lobby_waiting/$lobbyId"
    }
    object Game : Screen("game/{lobbyId}") {
        fun createRoute(lobbyId: String) = "game/$lobbyId"
    }
    object EndGame : Screen("end_game/{lobbyId}")
}