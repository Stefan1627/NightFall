package com.nightfall.ui.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nightfall.domain.model.Player
import com.nightfall.ui.component.ErrorSnackbar
import com.nightfall.ui.component.PlayerCard
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.util.Constants

@Composable
fun LobbyWaitingScreen(
    lobbyId: String,
    currentUserId: String = "",
    lobbyViewModel: LobbyViewModel = hiltViewModel(),
    onStartGame: (String) -> Unit = {},
    onLeave: () -> Unit = {}
) {
    val lobbyState by lobbyViewModel.lobbyState.collectAsState()
    val players by lobbyViewModel.players.collectAsState()
    val errorMessage by lobbyViewModel.errorMessage.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    val lobby = (lobbyState as? LobbyUiState.Active)?.lobby
    val isHost = lobby?.hostId == currentUserId

    // Navigate to game screen when the host starts the game
    androidx.compose.runtime.LaunchedEffect(lobby?.status) {
        if (lobby?.status == "in_progress") {
            onStartGame(lobbyId)
        }
    }

    // Start observing lobby state and players when screen opens
    androidx.compose.runtime.LaunchedEffect(lobbyId) {
        lobbyViewModel.startObserving(lobbyId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            ErrorSnackbar(
                message = errorMessage,
                onDismiss = { lobbyViewModel.clearError() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Lobby Code Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lobby Code",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = lobbyId,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(lobbyId))
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy lobby code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player count badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${players.size} / ${Constants.MAX_PLAYERS} players",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player list
            Text(
                text = "Players",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players, key = { it.playerId }) { player ->
                    PlayerCard(
                        player = player,
                        isCurrentUser = player.playerId == currentUserId
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom actions
            if (isHost) {
                Button(
                    onClick = { lobbyViewModel.startGame() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = players.size >= Constants.MIN_PLAYERS
                ) {
                    Text(
                        text = if (players.size < Constants.MIN_PLAYERS)
                            "Need ${Constants.MIN_PLAYERS - players.size} more players"
                        else
                            "Start Game",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "Waiting for host to start...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun LobbyWaitingScreenPreview() {
    NightFallTheme {
        LobbyWaitingScreen(lobbyId = "ABC12345")
    }
}