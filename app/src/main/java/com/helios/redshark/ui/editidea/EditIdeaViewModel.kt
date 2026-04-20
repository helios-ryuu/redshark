package com.helios.redshark.ui.editidea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.UpdateIdeaInput
import com.helios.redshark.domain.usecase.idea.GetIdeaDetailUseCase
import com.helios.redshark.domain.usecase.idea.UpdateIdeaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface EditIdeaUiState {
    data object Idle : EditIdeaUiState
    data object Loading : EditIdeaUiState
    data class Loaded(val idea: Idea) : EditIdeaUiState
    /** TC-C05: mutation succeeded — screen navigates back to detail. */
    data object Success : EditIdeaUiState
    data class ValidationError(val message: String) : EditIdeaUiState
    data class Error(val message: String) : EditIdeaUiState
}

@HiltViewModel
class EditIdeaViewModel @Inject constructor(
    private val getIdeaDetailUseCase: GetIdeaDetailUseCase,
    private val updateIdeaUseCase: UpdateIdeaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditIdeaUiState>(EditIdeaUiState.Idle)
    val uiState: StateFlow<EditIdeaUiState> = _uiState.asStateFlow()

    fun loadIdea(id: UUID) {
        viewModelScope.launch {
            _uiState.value = EditIdeaUiState.Loading
            _uiState.value = try {
                EditIdeaUiState.Loaded(getIdeaDetailUseCase(id))
            } catch (e: AppException) {
                EditIdeaUiState.Error(e.message ?: "Không tải được idea.")
            }
        }
    }

    fun saveIdea(id: UUID, title: String, description: String?, tagIds: List<UUID> = emptyList()) {
        if (_uiState.value is EditIdeaUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = EditIdeaUiState.Loading
            _uiState.value = try {
                updateIdeaUseCase(id, UpdateIdeaInput(title, description, tagIds))
                EditIdeaUiState.Success
            } catch (e: AppException.ValidationException) {
                EditIdeaUiState.ValidationError(e.message ?: "Dữ liệu không hợp lệ.")
            } catch (e: AppException) {
                EditIdeaUiState.Error(e.message ?: "Lưu thất bại. Vui lòng thử lại.")
            }
        }
    }

    fun resetState() {
        _uiState.value = EditIdeaUiState.Idle
    }
}
