package com.helios.redshark.ui.editissue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.domain.model.UpdateIssueInput
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.issue.GetIssueDetailUseCase
import com.helios.redshark.domain.usecase.issue.UpdateIssueUseCase
import com.helios.redshark.domain.usecase.user.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface EditIssueUiState {
    data object Idle : EditIssueUiState
    data object Loading : EditIssueUiState
    data class Loaded(val issue: Issue) : EditIssueUiState
    /** TC-C11: mutation OK — screen pops back to issue detail. */
    data object Success : EditIssueUiState
    data class ValidationError(val message: String) : EditIssueUiState
    data class Error(val message: String) : EditIssueUiState
}

@HiltViewModel
class EditIssueViewModel @Inject constructor(
    private val getIssueDetailUseCase: GetIssueDetailUseCase,
    private val updateIssueUseCase: UpdateIssueUseCase,
    private val getUsersUseCase: GetUsersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditIssueUiState>(EditIssueUiState.Idle)
    val uiState: StateFlow<EditIssueUiState> = _uiState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    fun loadIssue(id: UUID) {
        viewModelScope.launch {
            _uiState.value = EditIssueUiState.Loading
            _uiState.value = try {
                EditIssueUiState.Loaded(getIssueDetailUseCase(id))
            } catch (e: AppException) {
                EditIssueUiState.Error(e.message ?: "Không tải được issue.")
            }
        }
        viewModelScope.launch {
            val result = getUsersUseCase()
            if (result is Result.Success) _users.value = result.data
        }
    }

    fun saveIssue(
        id: UUID,
        title: String,
        description: String?,
        priority: IssuePriority,
        assigneeId: String?
    ) {
        if (_uiState.value is EditIssueUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = EditIssueUiState.Loading
            _uiState.value = try {
                updateIssueUseCase(id, UpdateIssueInput(title, description, priority, assigneeId))
                EditIssueUiState.Success
            } catch (e: AppException.ValidationException) {
                EditIssueUiState.ValidationError(e.message ?: "Dữ liệu không hợp lệ.")
            } catch (e: AppException) {
                EditIssueUiState.Error(e.message ?: "Lưu thất bại. Vui lòng thử lại.")
            }
        }
    }

    fun resetState() {
        _uiState.value = EditIssueUiState.Idle
    }
}
