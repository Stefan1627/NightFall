package com.nightfall.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nightfall.ui.component.PlayerCard
import com.nightfall.ui.component.RoleBadge
import com.nightfall.ui.theme.*
import com.nightfall.roles.RoleRegistry

@Composable
fun EndGameScreen(gameViewModel: GameViewModel, onPlayAgain: () -> Unit = {}, onLeave: () -> Unit = {}) {
    val gameState by gameViewModel.gameState.collectAsState()
    val players by gameViewModel.players.collectAsState()
    val isHost = gameViewModel.isHost
    val winner = gameState.winner
    val winIcon = if (winner == "mafia") "🔪" else "🏘️"
    val winText = if (winner == "mafia") "Mafia Wins!" else "Village Wins!"
    val winColor = if (winner == "mafia") MafiaRed else VillageBlue

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Text(winIcon, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(winText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = winColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Game over after ${gameState.round} rounds", style = MaterialTheme.typography.bodyLarge, color = MoonlightSilver)
        Spacer(Modifier.height(24.dp))
        Text("All Roles Revealed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OffWhite)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players, key = { it.playerId }) { player ->
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        PlayerCard(player, player.playerId == gameViewModel.currentUserId)
                    }
                    Spacer(Modifier.width(8.dp))
                    val role = player.role?.let { RoleRegistry.getRole(it) }
                    if (role != null) { RoleBadge(role, true) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isHost) {
                Button(onPlayAgain, Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Play Again")
                }
            }
            OutlinedButton(onLeave, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Leave")
            }
        }
    }
}