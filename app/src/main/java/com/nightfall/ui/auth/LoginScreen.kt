package com.nightfall.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nightfall.ui.theme.NightFallTheme

@Composable
fun LoginScreen(
    vm: AuthViewModel = hiltViewModel(),
    onLoggedIn: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            onLoggedIn()
            vm.onEvent(LoginEvent.SuccessConsumed)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome back", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = { vm.onEvent(LoginEvent.EmailChanged(it)) },
            label = { Text("Email") },
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
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { vm.onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
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
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.onEvent(LoginEvent.Submit) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Sign in", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("Create account", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
        }
        if (state.error != null) {
            Spacer(Modifier.height(16.dp))
            Snackbar { Text(state.error!!) }
        }
    }
}

@Preview
@Composable
private fun LoginPreview() { NightFallTheme { LoginScreen() } }