package com.nightfall.ui.auth

sealed class SignUpEvent {
    data class FullNameChanged(val fullName: String) : SignUpEvent()
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    object Submit : SignUpEvent()
    object SuccessConsumed : SignUpEvent()
}
