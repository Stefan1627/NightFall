package com.nightfall.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightfall.core.result.Result
import com.nightfall.core.session.SessionManager
import com.nightfall.domain.model.User
import com.nightfall.domain.usecase.auth.LoginUseCase
import com.nightfall.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

// Events used by LoginScreen
sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object Submit : LoginEvent()
    object SuccessConsumed : LoginEvent()
}

// Events used by RegisterScreen (SignUpScreen)
sealed class SignUpEvent {
    data class FullNameChanged(val name: String) : SignUpEvent()
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    object Submit : SignUpEvent()
    object SuccessConsumed : SignUpEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(sessionManager.isSessionActive())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is LoginEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            LoginEvent.Submit -> login(_state.value.email, _state.value.password)
            LoginEvent.SuccessConsumed -> _state.update { it.copy(success = false) }
        }
    }

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> _state.update { it.copy(fullName = event.name) }
            is SignUpEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is SignUpEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            SignUpEvent.Submit -> register(
                _state.value.email,
                _state.value.password,
                _state.value.fullName
            )
            SignUpEvent.SuccessConsumed -> _state.update { it.copy(success = false) }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            _uiState.value = AuthUiState.Loading
            when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    _state.update { it.copy(isLoading = false, success = true) }
                    _isLoggedIn.value = true
                }
                is Result.Error -> {
                    val message = result.message ?: result.exception.message ?: "Login failed"
                    _uiState.value = AuthUiState.Error(message)
                    _state.update { it.copy(isLoading = false, error = message) }
                }
                Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            _uiState.value = AuthUiState.Loading
            when (val result = registerUseCase(email, password, displayName)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    _state.update { it.copy(isLoading = false, success = true) }
                    _isLoggedIn.value = true
                }
                is Result.Error -> {
                    val message = result.message ?: result.exception.message ?: "Registration failed"
                    _uiState.value = AuthUiState.Error(message)
                    _state.update { it.copy(isLoading = false, error = message) }
                }
                Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            _uiState.value = AuthUiState.Idle
            _state.update { AuthFormState() }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        _state.update { it.copy(error = null) }
    }
}