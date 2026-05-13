package com.nightfall.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nightfall.ui.theme.MafiaRed
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.ui.theme.OffWhite
import kotlinx.coroutines.delay

@Composable
fun EliminationScreen(
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val eliminatedPlayerId by gameViewModel.eliminatedPlayerId.collectAsState()
    val players by gameViewModel.players.collectAsState()
    val gameState by gameViewModel.gameState.collectAsState()
    val isHost = gameViewModel.isHost

    val eliminatedPlayer = eliminatedPlayerId?.let { id ->
        players.find { it.playerId == id }
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    // Auto-advance after 5 seconds for non-host
    LaunchedEffect(Unit) {
        if (!isHost) {
            delay(5000)
            // Non-host just waits for host to advance
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Round ${gameState.round}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (eliminatedPlayer != null) {
                    Text(
                        text = "⚰️",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = eliminatedPlayer.displayName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MafiaRed,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "has been eliminated!",
                        style = MaterialTheme.typography.titleMedium,
                        color = OffWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role reveal
                    Text(
                        text = "They were a ${eliminatedPlayer.role?.replaceFirstChar { it.uppercase() } ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "🤝",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No one was eliminated\nthis round",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The vote was tied!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isHost) {
                    Button(
                        onClick = { gameViewModel.advancePhase() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Continue", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}