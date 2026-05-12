package com.helios.redshark.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.usecase.auth.CheckUsernameAvailabilityUseCase
import com.helios.redshark.domain.usecase.auth.SignUpEmailPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data class ValidationError(
        val displayNameError: String? = null,
        val usernameError: String? = null,
        val emailError: String? = null,
        val dobError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
    ) : RegisterUiState
    data object Success : RegisterUiState
    data class NetworkError(val message: String) : RegisterUiState
}

enum class UsernameAvailability { Idle, Checking, Available, Taken }

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val signUpEmailPasswordUseCase: SignUpEmailPasswordUseCase,
    private val checkUsernameAvailabilityUseCase: CheckUsernameAvailabilityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _usernameAvailability = MutableStateFlow(UsernameAvailability.Idle)
    val usernameAvailability: StateFlow<UsernameAvailability> = _usernameAvailability.asStateFlow()

    private var usernameCheckJob: Job? = null

    fun onUsernameChanged(username: String) {
        usernameCheckJob?.cancel()
        if (username.trim().length < 3) {
            _usernameAvailability.value = UsernameAvailability.Idle
            return
        }
        _usernameAvailability.value = UsernameAvailability.Checking
        usernameCheckJob = viewModelScope.launch {
            delay(300)
            when (val result = checkUsernameAvailabilityUseCase(username.trim())) {
                is Result.Success -> {
                    _usernameAvailability.value = if (result.data) {
                        UsernameAvailability.Available
                    } else {
                        UsernameAvailability.Taken
                    }
                }
                is Result.Error -> _usernameAvailability.value = UsernameAvailability.Idle
                is Result.Loading -> Unit
            }
        }
    }

    fun onSubmit(
        displayName: String,
        username: String,
        email: String,
        dateOfBirth: LocalDate?,
        password: String,
        confirmPassword: String,
    ) {
        if (dateOfBirth == null) {
            _uiState.value = RegisterUiState.ValidationError(dobError = "Date of birth is required")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.ValidationError(confirmPasswordError = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            when (val result = signUpEmailPasswordUseCase(displayName, username, email, dateOfBirth, password)) {
                is Result.Success -> _uiState.value = RegisterUiState.Success
                is Result.Error -> {
                    val ex = result.exception
                    when {
                        ex is AppException.ValidationException -> {
                            _uiState.value = RegisterUiState.ValidationError(
                                displayNameError = if (ex.field == "displayName") ex.message else null,
                                usernameError = if (ex.field == "username") ex.message else null,
                                emailError = if (ex.field == "email") ex.message else null,
                                dobError = if (ex.field == "dateOfBirth") ex.message else null,
                                passwordError = if (ex.field == "password") ex.message else null,
                            )
                        }
                        ex is AppException.ConflictException -> {
                            _uiState.value = if (ex.field == "email") {
                                RegisterUiState.ValidationError(emailError = ex.message)
                            } else {
                                RegisterUiState.ValidationError(usernameError = ex.message)
                            }
                        }
                        else -> {
                            Timber.e(ex)
                            _uiState.value = RegisterUiState.NetworkError(ex.message ?: "Registration failed")
                        }
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearError() {
        if (_uiState.value is RegisterUiState.NetworkError) {
            _uiState.value = RegisterUiState.Idle
        }
    }
}
