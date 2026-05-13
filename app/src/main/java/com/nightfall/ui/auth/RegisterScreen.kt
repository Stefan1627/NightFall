package com.nightfall.ui.auth

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
fun SignUpScreen(
    vm: AuthViewModel = hiltViewModel(),
    onSignInClick: () -> Unit = {}
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            onSignInClick()
            vm.onEvent(SignUpEvent.SuccessConsumed)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create your account", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = state.fullName,
            onValueChange = { vm.onEvent(SignUpEvent.FullNameChanged(it)) },
            label = { Text("Full name") },
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
            value = state.email,
            onValueChange = { vm.onEvent(SignUpEvent.EmailChanged(it)) },
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
            onValueChange = { vm.onEvent(SignUpEvent.PasswordChanged(it)) },
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
            onClick = { vm.onEvent(SignUpEvent.Submit) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Sign up", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSignInClick) {
            Text("Already have an account? Sign in", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.error != null) {
            Spacer(Modifier.height(16.dp))
            Snackbar { Text(state.error!!) }
        }
    }
}

@Preview
@Composable
private fun SignUpPreview() { NightFallTheme { SignUpScreen() } }