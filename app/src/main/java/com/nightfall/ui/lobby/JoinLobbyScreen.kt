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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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

@Composable
fun JoinLobbyScreen(
    lobbyViewModel: LobbyViewModel = hiltViewModel(),
    onLobbyJoined: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val lobbyState by lobbyViewModel.lobbyState.collectAsState()
    val lobbyId by lobbyViewModel.currentLobbyId.collectAsState()
    var lobbyCode by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lobbyState) {
        when (lobbyState) {
            is LobbyUiState.Active -> {
                if (lobbyId != null) {
                    onLobbyJoined(lobbyId!!)
                }
            }
            is LobbyUiState.Error -> {
                snackbarHostState.showSnackbar((lobbyState as LobbyUiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                text = "Join a Lobby",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = lobbyCode,
                onValueChange = { lobbyCode = it.uppercase() },
                label = { Text("Enter Lobby Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (lobbyCode.isNotBlank()) {
                        lobbyViewModel.joinLobby(lobbyCode.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = lobbyCode.isNotBlank() && lobbyState !is LobbyUiState.Loading
            ) {
                Text(
                    text = "Join",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        LoadingOverlay(isVisible = lobbyState is LobbyUiState.Loading)
    }
}

@Preview
@Composable
private fun JoinLobbyScreenPreview() {
    NightFallTheme {
        JoinLobbyScreen()
    }
}