package com.helios.redshark.ui.editidea

// File nay xu ly trang thai va giao dien nguoi dung cho mot tinh nang.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.error.AppException
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

// Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
sealed interface EditIdeaUiState {
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Idle : EditIdeaUiState
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Loading : EditIdeaUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class Loaded(val idea: Idea) : EditIdeaUiState
    /** TC-C05: mutation succeeded — screen navigates back to detail. */
    data object Success : EditIdeaUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class ValidationError(val message: String) : EditIdeaUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class NetworkError(val message: String) : EditIdeaUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class Error(val message: String) : EditIdeaUiState
}

// Quan ly state va goi use case de man hinh chi can render du lieu.
@HiltViewModel
class EditIdeaViewModel @Inject constructor(
    private val getIdeaDetailUseCase: GetIdeaDetailUseCase,
    private val updateIdeaUseCase: UpdateIdeaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditIdeaUiState>(EditIdeaUiState.Idle)
    val uiState: StateFlow<EditIdeaUiState> = _uiState.asStateFlow()

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun saveIdea(id: UUID, title: String, description: String?, tagIds: List<UUID> = emptyList()) {
        if (_uiState.value is EditIdeaUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = EditIdeaUiState.Loading
            _uiState.value = try {
                updateIdeaUseCase(id, UpdateIdeaInput(title, description, tagIds))
                EditIdeaUiState.Success
            } catch (e: AppException.ValidationException) {
                EditIdeaUiState.ValidationError(e.message ?: "Dữ liệu không hợp lệ.")
            } catch (e: AppException.NetworkException) {
                EditIdeaUiState.NetworkError(e.message ?: "Lỗi kết nối mạng.")
            } catch (e: AppException) {
                EditIdeaUiState.Error(e.message ?: "Lưu thất bại. Vui lòng thử lại.")
            }
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun resetState() {
        _uiState.value = EditIdeaUiState.Idle
    }
}
