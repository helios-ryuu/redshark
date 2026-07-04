package com.helios.redshark.ui.auth

// File nay xu ly trang thai va giao dien nguoi dung cho mot tinh nang.

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.firebase.GoogleSignInHelper
import com.helios.redshark.domain.usecase.auth.SignInEmailPasswordUseCase
import com.helios.redshark.domain.usecase.auth.SignInGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
sealed interface LoginUiState {
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Idle : LoginUiState
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Loading : LoginUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class ValidationError(
        val emailError: String? = null,
        val passwordError: String? = null,
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    ) : LoginUiState
    data object Success : LoginUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class NetworkError(val message: String) : LoginUiState
}

// Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
sealed class LoginDestination {
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Home : LoginDestination()
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object ProfileSetup : LoginDestination()
}

// Quan ly state va goi use case de man hinh chi can render du lieu.
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInEmailPasswordUseCase: SignInEmailPasswordUseCase,
    private val signInGoogleUseCase: SignInGoogleUseCase,
    private val googleSignInHelper: GoogleSignInHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _destination = MutableStateFlow<LoginDestination?>(null)
    val destination: StateFlow<LoginDestination?> = _destination.asStateFlow()

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun signInEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = signInEmailPasswordUseCase(email, password)) {
                is Result.Success -> {
                    val user = result.data
                    _destination.value = if (user.displayName.trim().length < 3) {
                        LoginDestination.ProfileSetup
                    } else {
                        LoginDestination.Home
                    }
                    _uiState.value = LoginUiState.Success
                }
                is Result.Error -> {
                    val ex = result.exception
                    if (ex is AppException.ValidationException) {
                        _uiState.value = LoginUiState.ValidationError(
                            emailError = if (ex.field == "email") ex.message else null,
                            passwordError = if (ex.field == "password") ex.message else null,
                        )
                    } else {
                        Timber.e(ex)
                        _uiState.value = LoginUiState.NetworkError(ex.message ?: "Sign-in failed")
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun signInGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val tokenResult = googleSignInHelper.requestGoogleIdToken(activityContext)) {
                is Result.Success -> {
                    when (val signInResult = signInGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            val user = signInResult.data
                            _destination.value = if (user.displayName.trim().length < 3) {
                                LoginDestination.ProfileSetup
                            } else {
                                LoginDestination.Home
                            }
                            _uiState.value = LoginUiState.Success
                        }
                        is Result.Error -> {
                            Timber.e(signInResult.exception)
                            _uiState.value = LoginUiState.NetworkError(
                                signInResult.exception.message ?: "Google sign-in failed"
                            )
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.NetworkError(
                        tokenResult.exception.message ?: "Google sign-in failed"
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun clearError() {
        if (_uiState.value is LoginUiState.NetworkError || _uiState.value is LoginUiState.ValidationError) {
            _uiState.value = LoginUiState.Idle
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun onNavigationHandled() {
        _destination.value = null
    }
}
