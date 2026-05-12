package com.helios.redshark.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.firebase.GoogleSignInHelper
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.auth.CompleteFirstProfileUseCase
import com.helios.redshark.domain.usecase.auth.ObserveAuthStateUseCase
import com.helios.redshark.domain.usecase.auth.SignInGoogleUseCase
import com.helios.redshark.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val navigateTo: AuthDestination? = null,
)

sealed class AuthDestination {
    data object Home : AuthDestination()
    data object ProfileSetup : AuthDestination()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInGoogleUseCase: SignInGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val completeFirstProfileUseCase: CompleteFirstProfileUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val googleSignInHelper: GoogleSignInHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun onSignInClicked(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val tokenResult = googleSignInHelper.requestGoogleIdToken(activityContext)) {
                is Result.Success -> {
                    when (val signInResult = signInGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            val user = signInResult.data
                            val destination = if (user.displayName.trim().length < 3) {
                                AuthDestination.ProfileSetup
                            } else {
                                AuthDestination.Home
                            }
                            _uiState.update {
                                it.copy(isLoading = false, user = user, navigateTo = destination)
                            }
                        }
                        is Result.Error -> {
                            Timber.e(signInResult.exception)
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = signInResult.exception.message)
                            }
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = tokenResult.exception.message)
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onCompleteProfile(userId: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = completeFirstProfileUseCase(userId, displayName)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, navigateTo = AuthDestination.Home)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.exception.message)
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onSignOutClicked() {
        viewModelScope.launch {
            signOutUseCase()
            _uiState.update { AuthUiState() }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateTo = null) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
