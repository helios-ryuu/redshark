package com.helios.redshark.ui.auth

// File nay xu ly trang thai va giao dien nguoi dung cho mot tinh nang.

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

// Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
sealed interface RegisterUiState {
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Idle : RegisterUiState
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Loading : RegisterUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class ValidationError(
        val displayNameError: String? = null,
        val usernameError: String? = null,
        val emailError: String? = null,
        val dobError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    ) : RegisterUiState
    data object Success : RegisterUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class NetworkError(val message: String) : RegisterUiState
}

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class UsernameAvailability { Idle, Checking, Available, Taken, CheckFailed }

// Quan ly state va goi use case de man hinh chi can render du lieu.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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
                is Result.Error -> _usernameAvailability.value = UsernameAvailability.CheckFailed
                is Result.Loading -> Unit
            }
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun clearError() {
        if (_uiState.value is RegisterUiState.NetworkError) {
            _uiState.value = RegisterUiState.Idle
        }
    }
}
