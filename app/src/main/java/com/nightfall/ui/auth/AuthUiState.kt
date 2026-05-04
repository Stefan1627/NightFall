package com.nightfall.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
