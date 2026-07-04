package com.helios.redshark.ui.createidea

// File nay xu ly trang thai va giao dien nguoi dung cho mot tinh nang.

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

// Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
sealed interface CreateIdeaUiState {
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Idle : CreateIdeaUiState
    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    data object Loading : CreateIdeaUiState
    // Model du lieu nay giu cac truong can truyen giua cac lop.
    data class Success(val ideaId: UUID) : CreateIdeaUiState

    // Sealed type nay liet ke cac trang thai hop le ma code can xu ly du.
    sealed interface Failure : CreateIdeaUiState {
        // Model du lieu nay giu cac truong can truyen giua cac lop.
        data class ValidationError(val message: String) : Failure
        /** TC-C21: network gone while submitting — form data must be preserved in the VM. */
        data class NetworkError(val message: String) : Failure
        // Model du lieu nay giu cac truong can truyen giua cac lop.
        data class GenericError(val message: String) : Failure
    }
}

// Quan ly state va goi use case de man hinh chi can render du lieu.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun resetState() {
        _uiState.value = CreateIdeaUiState.Idle
    }
}
