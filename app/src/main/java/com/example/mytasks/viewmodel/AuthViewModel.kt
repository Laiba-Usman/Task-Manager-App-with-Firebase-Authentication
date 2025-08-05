package com.example.mytasks.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytasks.data.datastore.UserPreferencesRepository
import com.example.mytasks.repository.AuthRepository
import com.example.mytasks.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Combine Firebase auth state with local preferences
    val isLoggedIn: StateFlow<Boolean> = combine(
        authRepository.getCurrentUserFlow(),
        userPreferencesRepository.isLoggedIn
    ) { firebaseUser, localIsLoggedIn ->
        val result = firebaseUser != null && localIsLoggedIn
        Log.d("AuthViewModel", "Login state: Firebase=${firebaseUser?.email}, Local=$localIsLoggedIn, Result=$result")
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val userName = userPreferencesRepository.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val userEmail = userPreferencesRepository.userEmail
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.UpdateEmail -> updateEmail(event.email)
            is AuthEvent.UpdatePassword -> updatePassword(event.password)
            is AuthEvent.UpdateName -> updateName(event.name)
            is AuthEvent.UpdateConfirmPassword -> updateConfirmPassword(event.confirmPassword)
            AuthEvent.Login -> login()
            AuthEvent.Register -> register()
            AuthEvent.Logout -> logout()
            AuthEvent.ClearError -> clearError()
            AuthEvent.TogglePasswordVisibility -> togglePasswordVisibility()
            AuthEvent.ToggleConfirmPasswordVisibility -> toggleConfirmPasswordVisibility()
        }
    }

    private fun updateEmail(email: String) {
        _authState.value = _authState.value.copy(email = email)
    }

    private fun updatePassword(password: String) {
        _authState.value = _authState.value.copy(password = password)
    }

    private fun updateName(name: String) {
        _authState.value = _authState.value.copy(name = name)
    }

    private fun updateConfirmPassword(confirmPassword: String) {
        _authState.value = _authState.value.copy(confirmPassword = confirmPassword)
    }

    private fun togglePasswordVisibility() {
        _authState.value = _authState.value.copy(isPasswordVisible = !_authState.value.isPasswordVisible)
    }

    private fun toggleConfirmPasswordVisibility() {
        _authState.value = _authState.value.copy(isConfirmPasswordVisible = !_authState.value.isConfirmPasswordVisible)
    }

    private fun login() {
        if (_authState.value.email.isBlank() || _authState.value.password.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Please fill in all fields")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val result = authRepository.login(
                    email = _authState.value.email.trim(),
                    password = _authState.value.password
                )
                _authState.value = _authState.value.copy(isLoading = false)
                when (result) {
                    is AuthResult.Success -> {
                        // Get current user data and save to preferences
                        val userData = authRepository.getCurrentUserData()
                        userPreferencesRepository.saveLoginState(
                            isLoggedIn = true,
                            name = userData?.name ?: "",
                            email = userData?.email ?: _authState.value.email.trim()
                        )
                        Log.d("AuthViewModel", "Login successful")
                    }
                    is AuthResult.Error -> {
                        _authState.value = _authState.value.copy(errorMessage = result.message)
                        Log.e("AuthViewModel", "Login failed: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login exception", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )
            }
        }
    }

    private fun register() {
        if (!validateInput()) return

        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val result = authRepository.register(
                    email = _authState.value.email.trim(),
                    password = _authState.value.password,
                    name = _authState.value.name.trim()
                )
                _authState.value = _authState.value.copy(isLoading = false)
                when (result) {
                    is AuthResult.Success -> {
                        userPreferencesRepository.saveLoginState(
                            isLoggedIn = true,
                            name = _authState.value.name.trim(),
                            email = _authState.value.email.trim()
                        )
                        Log.d("AuthViewModel", "Registration successful")
                    }
                    is AuthResult.Error -> {
                        _authState.value = _authState.value.copy(errorMessage = result.message)
                        Log.e("AuthViewModel", "Registration failed: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration exception", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Registration failed: ${e.message}"
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                userPreferencesRepository.clearLoginState()
                Log.d("AuthViewModel", "Logout successful")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout failed", e)
            }
        }
    }

    private fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    private fun validateInput(): Boolean {
        val state = _authState.value
        return when {
            state.name.isBlank() -> {
                _authState.value = state.copy(errorMessage = "Please enter your name")
                false
            }
            state.email.isBlank() -> {
                _authState.value = state.copy(errorMessage = "Please enter your email")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                _authState.value = state.copy(errorMessage = "Please enter a valid email")
                false
            }
            state.password.isBlank() -> {
                _authState.value = state.copy(errorMessage = "Please enter your password")
                false
            }
            state.password.length < 6 -> {
                _authState.value = state.copy(errorMessage = "Password must be at least 6 characters")
                false
            }
            state.confirmPassword.isBlank() -> {
                _authState.value = state.copy(errorMessage = "Please confirm your password")
                false
            }
            state.password != state.confirmPassword -> {
                _authState.value = state.copy(errorMessage = "Passwords do not match")
                false
            }
            else -> true
        }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
)

sealed class AuthEvent {
    data class UpdateEmail(val email: String) : AuthEvent()
    data class UpdatePassword(val password: String) : AuthEvent()
    data class UpdateName(val name: String) : AuthEvent()
    data class UpdateConfirmPassword(val confirmPassword: String) : AuthEvent()
    object Login : AuthEvent()
    object Register : AuthEvent()
    object Logout : AuthEvent()
    object ClearError : AuthEvent()
    object TogglePasswordVisibility : AuthEvent()
    object ToggleConfirmPasswordVisibility : AuthEvent()
}