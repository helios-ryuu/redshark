package com.helios.redshark.ui.createidea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateIdeaInput
import com.helios.redshark.domain.usecase.idea.CreateIdeaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface CreateIdeaUiState {
    data object Idle : CreateIdeaUiState
    data object Loading : CreateIdeaUiState
    data class Success(val ideaId: UUID) : CreateIdeaUiState

    sealed interface Failure : CreateIdeaUiState {
        data class ValidationError(val message: String) : Failure
        /** TC-C21: network gone while submitting — form data must be preserved in the VM. */
        data class NetworkError(val message: String) : Failure
        data class GenericError(val message: String) : Failure
    }
}

@HiltViewModel
class CreateIdeaViewModel @Inject constructor(
    private val createIdeaUseCase: CreateIdeaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateIdeaUiState>(CreateIdeaUiState.Idle)
    val uiState: StateFlow<CreateIdeaUiState> = _uiState.asStateFlow()

    /**
     * TC-C21: on NetworkException the state becomes [Failure.NetworkError] and the VM retains
     * [draftTitle] / [draftDescription] so the screen can re-populate the form on retry.
     */
    var draftTitle: String = ""
        private set
    var draftDescription: String? = null
        private set

    fun createIdea(title: String, description: String?, tagIds: List<UUID> = emptyList()) {
        if (_uiState.value is CreateIdeaUiState.Loading) return

        draftTitle = title
        draftDescription = description

        viewModelScope.launch {
            _uiState.value = CreateIdeaUiState.Loading
            _uiState.value = try {
                val idea = createIdeaUseCase(CreateIdeaInput(title, description, tagIds))
                CreateIdeaUiState.Success(idea.id)
            } catch (e: AppException.ValidationException) {
                CreateIdeaUiState.Failure.ValidationError(e.message ?: "Dữ liệu không hợp lệ.")
            } catch (e: AppException.NetworkException) {
                // TC-C21: distinct state — UI reads draftTitle/draftDescription to refill the form
                CreateIdeaUiState.Failure.NetworkError("Không có kết nối. Kiểm tra mạng và thử lại.")
            } catch (e: AppException) {
                CreateIdeaUiState.Failure.GenericError(e.message ?: "Đã xảy ra lỗi.")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateIdeaUiState.Idle
    }
}
