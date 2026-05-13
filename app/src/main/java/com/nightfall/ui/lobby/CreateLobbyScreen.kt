package com.nightfall.ui.lobby

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nightfall.ui.component.LoadingOverlay
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.util.Constants
import androidx.compose.foundation.layout.Row

@Composable
fun CreateLobbyScreen(
    lobbyViewModel: LobbyViewModel = hiltViewModel(),
    onLobbyCreated: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val lobbyState by lobbyViewModel.lobbyState.collectAsState()
    val lobbyId by lobbyViewModel.currentLobbyId.collectAsState()
    var selectedMode by remember { mutableStateOf(Constants.GAME_MODE_CLASSIC) }

    LaunchedEffect(lobbyState) {
        if (lobbyState is LobbyUiState.Active && lobbyId != null) {
            onLobbyCreated(lobbyId!!)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create a Lobby",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Select Game Mode",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Classic mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == Constants.GAME_MODE_CLASSIC,
                    onClick = { selectedMode = Constants.GAME_MODE_CLASSIC },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Classic",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Standard roles and rules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chaos mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == Constants.GAME_MODE_CHAOS,
                    onClick = { selectedMode = Constants.GAME_MODE_CHAOS },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Chaos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "More mafia, more chaos!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { lobbyViewModel.createLobby(selectedMode) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = lobbyState !is LobbyUiState.Loading
            ) {
                Text(
                    text = "Create Lobby",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (lobbyState is LobbyUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (lobbyState as LobbyUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        LoadingOverlay(isVisible = lobbyState is LobbyUiState.Loading)
    }
}

@Preview
@Composable
private fun CreateLobbyScreenPreview() {
    NightFallTheme {
        CreateLobbyScreen()
    }
}