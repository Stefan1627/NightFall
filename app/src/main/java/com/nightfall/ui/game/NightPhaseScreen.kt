package com.nightfall.ui.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightfall.ui.component.PhaseTimer
import com.nightfall.ui.component.PlayerCard
import com.nightfall.ui.component.RoleBadge
import com.nightfall.ui.theme.MoonlightSilver
import com.nightfall.ui.theme.NightSky
import com.nightfall.util.Constants
import kotlinx.coroutines.delay

@Composable
fun NightPhaseScreen(gameViewModel: GameViewModel, modifier: Modifier = Modifier) {
    val players by gameViewModel.players.collectAsState()
    val myRole by gameViewModel.myRole.collectAsState()
    val hasSubmitted by gameViewModel.hasSubmittedNightAction.collectAsState()
    val remaining by gameViewModel.phaseRemainingMs.collectAsState()
    val roleRevealShown by gameViewModel.roleRevealShown.collectAsState()
    val currentUserId = gameViewModel.currentUserId
    val hasAction = myRole?.hasNightAction == true
    val alive = players.filter { it.isAlive && it.playerId != currentUserId }

    Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().background(NightSky)) {
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

    // Role reveal overlay — shown once per game start
    if (!roleRevealShown && myRole != null) {
        RoleRevealOverlay(
            roleDisplayName = myRole!!.displayName,
            roleInfo = myRole!!.getRoleInfo(),
            faction = myRole!!.faction,
            onDismiss = { gameViewModel.dismissRoleReveal() }
        )
    }
    } // end Box
}

@Composable
private fun RoleRevealOverlay(
    roleDisplayName: String,
    roleInfo: String,
    faction: String,
    onDismiss: () -> Unit
) {
    var flipped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        flipped = true
    }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 0f else 180f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front — role revealed
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Role",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = roleDisplayName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = if (faction == "mafia") MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = faction.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = roleInfo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "I understand my role",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Back — mystery card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { rotationY = 180f },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("?", style = MaterialTheme.typography.displayLarge, color = MoonlightSilver)
                    }
                }
            }
        }
    }
}