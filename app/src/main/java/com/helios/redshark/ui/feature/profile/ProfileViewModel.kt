package com.helios.redshark.ui.feature.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.auth.UpdateProfileUseCase
import com.helios.redshark.domain.usecase.auth.UploadAvatarUseCase
import com.helios.redshark.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isSaving: Boolean = false,
    val isOwner: Boolean = false,
    val savedSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String, currentUserId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = profileRepository.getProfile(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            isOwner = result.data.id == currentUserId,
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.exception.message)
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onSaveProfile(userId: String, displayName: String, bio: String, skills: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = updateProfileUseCase(userId, displayName, bio.ifBlank { null }, skills)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isSaving = false, user = result.data, savedSuccess = true)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = result.exception.message)
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onUploadAvatar(context: Context, userId: String, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: run {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = "Cannot read image file")
                        }
                        return@launch
                    }
                when (val result = uploadAvatarUseCase(userId, bytes, mimeType)) {
                    is Result.Success -> {
                        when (val saveResult = profileRepository.updateAvatarUrl(userId, result.data)) {
                            is Result.Success -> _uiState.update { it.copy(isSaving = false, user = saveResult.data) }
                            is Result.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = saveResult.exception.message) }
                            is Result.Loading -> Unit
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = result.exception.message)
                        }
                    }
                    is Result.Loading -> Unit
                }
            } catch (e: Exception) {
                Timber.e(e, "Avatar upload failed")
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Avatar upload failed")
                }
            }
        }
    }

    fun onSavedHandled() {
        _uiState.update { it.copy(savedSuccess = false) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
