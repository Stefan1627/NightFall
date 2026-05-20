package com.nightfall.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun ErrorSnackbar(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (!message.isNullOrEmpty()) {
            hostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onDismiss()
        }
    }

    SnackbarHost(hostState = hostState, modifier = modifier)
}
