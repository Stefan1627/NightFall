package com.nightfall.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightfall.core.result.onError
import com.nightfall.core.result.onSuccess
import com.nightfall.domain.usecase.auth.GetCurrentUserUseCase
import com.nightfall.domain.usecase.auth.LoginUseCase
import com.nightfall.domain.usecase.auth.LogoutUseCase
import com.nightfall.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(getCurrentUserUseCase() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onEvent(event: Any) {
        when (event) {
            is LoginEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is LoginEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            is LoginEvent.Submit -> login()
            is LoginEvent.SuccessConsumed -> _state.update { it.copy(success = false) }
            is SignUpEvent.FullNameChanged -> _state.update { it.copy(fullName = event.fullName) }
            is SignUpEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is SignUpEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            is SignUpEvent.Submit -> register()
            is SignUpEvent.SuccessConsumed -> _state.update { it.copy(success = false) }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            loginUseCase(_state.value.email, _state.value.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, success = true) }
                    _isLoggedIn.value = true
                }
                .onError { _, msg ->
                    _state.update { it.copy(isLoading = false, error = msg ?: "Login failed") }
                }
        }
    }

    private fun register() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            registerUseCase(_state.value.email, _state.value.password, _state.value.fullName)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, success = true) }
                    _isLoggedIn.value = true
                }
                .onError { _, msg ->
                    _state.update { it.copy(isLoading = false, error = msg ?: "Registration failed") }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            logoutUseCase()
            _isLoggedIn.value = false
        }
    }
}
