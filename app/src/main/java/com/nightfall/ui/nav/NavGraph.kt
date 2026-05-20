package com.nightfall.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nightfall.core.session.SessionManager
import com.nightfall.domain.model.GamePhase
import com.nightfall.ui.auth.AuthViewModel
import com.nightfall.ui.auth.LoginScreen
import com.nightfall.ui.auth.SignUpScreen
import com.nightfall.ui.game.*
import com.nightfall.ui.home.HomeScreen
import com.nightfall.ui.lobby.CreateLobbyScreen
import com.nightfall.ui.lobby.JoinLobbyScreen
import com.nightfall.ui.lobby.LobbyWaitingScreen

@Composable
fun NightfallNavGraph(
    navController: NavHostController = rememberNavController(),
    sessionManager: SessionManager
) {
    val startDest = if (sessionManager.isSessionActive()) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDest) {
        composable(Screen.Login.route) {
            val authVm: AuthViewModel = hiltViewModel()
            LoginScreen(
                vm = authVm,
                onLoggedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onCreateAccount = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            val authVm: AuthViewModel = hiltViewModel()
            SignUpScreen(
                vm = authVm,
                onSignInClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCreateLobby = { navController.navigate(Screen.CreateLobby.route) },
                onJoinLobby = {
                    navController.navigate(Screen.JoinLobby.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateLobby.route) {
            CreateLobbyScreen(
                onLobbyCreated = { lobbyId ->
                    navController.navigate(Screen.LobbyWaiting.createRoute(lobbyId)) {
                        popUpTo(Screen.CreateLobby.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.JoinLobby.route) {
            JoinLobbyScreen(
                onLobbyJoined = { lobbyId ->
                    navController.navigate(Screen.LobbyWaiting.createRoute(lobbyId)) {
                        popUpTo(Screen.JoinLobby.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LobbyWaiting.route,
            arguments = listOf(navArgument("lobbyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: ""
            LobbyWaitingScreen(
                lobbyId = lobbyId,
                currentUserId = sessionManager.getCurrentUserId() ?: "",
                onStartGame = { id ->
                    navController.navigate(Screen.Game.createRoute(id)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onLeave = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("lobbyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: ""
            val gameVm: GameViewModel = hiltViewModel()
            val phase by gameVm.currentPhase.collectAsState()

            when (phase) {
                is GamePhase.Night -> NightPhaseScreen(gameVm)
                is GamePhase.Day -> DayPhaseScreen(gameVm)
                is GamePhase.Voting -> VotingScreen(gameVm)
                is GamePhase.Elimination,
                is GamePhase.CheckWin -> EliminationScreen(gameVm)
                is GamePhase.EndGame -> EndGameScreen(
                    gameVm,
                    onPlayAgain = {
                        gameVm.resetGame {
                            navController.navigate(Screen.LobbyWaiting.createRoute(lobbyId)) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    },
                    onLeave = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
                else -> DayPhaseScreen(gameVm)
            }
        }

        composable(Screen.EndGame.route) {
            val gameVm: GameViewModel = hiltViewModel()
            EndGameScreen(
                gameVm,
                onLeave = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}