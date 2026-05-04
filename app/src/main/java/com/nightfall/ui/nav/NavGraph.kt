package com.nightfall.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { }
    }
}
