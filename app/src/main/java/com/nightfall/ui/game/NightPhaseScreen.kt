package com.nightfall.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nightfall.ui.component.PhaseTimer
import com.nightfall.ui.component.PlayerCard
import com.nightfall.ui.component.RoleBadge
import com.nightfall.ui.theme.MoonlightSilver
import com.nightfall.ui.theme.NightSky
import com.nightfall.util.Constants

@Composable
fun NightPhaseScreen(gameViewModel: GameViewModel, modifier: Modifier = Modifier) {
    val players by gameViewModel.players.collectAsState()
    val myRole by gameViewModel.myRole.collectAsState()
    val hasSubmitted by gameViewModel.hasSubmittedNightAction.collectAsState()
    val remaining by gameViewModel.phaseRemainingMs.collectAsState()
    val currentUserId = gameViewModel.currentUserId
    val hasAction = myRole?.hasNightAction == true
    val alive = players.filter { it.isAlive && it.playerId != currentUserId }

    Column(modifier = modifier.fillMaxSize().background(NightSky)) {
        Row(Modifier.fillMaxWidth().background(Color.Black.copy(0.3f)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Nightlight, null, tint = MoonlightSilver, modifier = Modifier.size(28.dp))
                    Text(" Night Phase", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MoonlightSilver)
                }
                Text("The town sleeps...", style = MaterialTheme.typography.bodySmall, color = MoonlightSilver.copy(0.7f))
            }
            PhaseTimer(remaining, Constants.NIGHT_PHASE_DURATION_MS)
        }
        Spacer(Modifier.height(16.dp))
        if (!hasAction) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💤", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text("You are sleeping...", style = MaterialTheme.typography.headlineSmall, color = MoonlightSilver, textAlign = TextAlign.Center)
                }
            }
        } else if (hasSubmitted) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✓", style = MaterialTheme.typography.displayLarge, color = MoonlightSilver)
                    Spacer(Modifier.height(16.dp))
                    Text("Action submitted", style = MaterialTheme.typography.headlineSmall, color = MoonlightSilver)
                }
            }
        } else {
            Text("Choose your target", style = MaterialTheme.typography.titleMedium, color = MoonlightSilver, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alive, key = { it.playerId }) { player ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(0.3f)).padding(12.dp)) {
                        PlayerCard(player, false)
                        Spacer(Modifier.height(4.dp))
                        Button({ gameViewModel.submitNightAction(player.playerId) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(0.8f))) {
                            Text("Select Target")
                        }
                    }
                }
            }
        }
        if (myRole != null) {
            Box(Modifier.fillMaxWidth().background(Color.Black.copy(0.5f)).padding(12.dp), contentAlignment = Alignment.Center) {
                RoleBadge(myRole!!, false)
            }
        }
    }
}