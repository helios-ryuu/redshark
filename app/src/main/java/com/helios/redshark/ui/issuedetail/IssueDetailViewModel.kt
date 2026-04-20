package com.helios.redshark.ui.issuedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssueStatus
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import com.helios.redshark.domain.usecase.issue.GetIssueDetailUseCase
import com.helios.redshark.domain.usecase.issue.SoftDeleteIssueUseCase
import com.helios.redshark.domain.usecase.issue.UpdateIssueStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class IssueDetailUiState(
    val issue: Issue? = null,
    val isLoading: Boolean = false,
    /**
     * TC-C12: derived once at load time — UI binds visibility of Edit/Delete controls
     * to this flag instead of comparing IDs directly in Composable code.
     */
    val canEdit: Boolean = false,
    val statusUpdateState: StatusUpdateState = StatusUpdateState.Idle,
    val errorMessage: String? = null,
    val navigateBack: Boolean = false,
    /** TC-C16: loaded after issue to show assignee avatar. */
    val assigneeUser: User? = null,
)

sealed interface StatusUpdateState {
    data object Idle : StatusUpdateState
    data object Updating : StatusUpdateState
    data object Success : StatusUpdateState
    /** TC-C14: carries the human-readable reason for invalid transition. */
    data class InvalidTransition(val message: String) : StatusUpdateState
    data class Error(val message: String) : StatusUpdateState
}

@HiltViewModel
class IssueDetailViewModel @Inject constructor(
    private val getIssueDetailUseCase: GetIssueDetailUseCase,
    private val updateIssueStatusUseCase: UpdateIssueStatusUseCase,
    private val softDeleteIssueUseCase: SoftDeleteIssueUseCase,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IssueDetailUiState())
    val uiState: StateFlow<IssueDetailUiState> = _uiState.asStateFlow()

    fun loadIssue(issueId: UUID, currentUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val issue = getIssueDetailUseCase(issueId)
                onIssueLoaded(issue, currentUserId)
            } catch (e: AppException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** Call after fetching [issue] so the VM derives [canEdit] once. */
    fun onIssueLoaded(issue: Issue, currentUserId: String) {
        _uiState.update {
            it.copy(
                issue = issue,
                isLoading = false,
                canEdit = issue.authorId == currentUserId || issue.assigneeId == currentUserId
            )
        }
        issue.assigneeId?.let { loadAssigneeProfile(it) }
    }

    private fun loadAssigneeProfile(userId: String) {
        viewModelScope.launch {
            val result = profileRepository.getProfile(userId)
            if (result is Result.Success) {
                _uiState.update { it.copy(assigneeUser = result.data) }
            }
        }
    }

    /**
     * TC-C13/C14: delegates validation to [UpdateIssueStatusUseCase] which checks
     * [ISSUE_STATE_MACHINE]. Invalid transitions surface as [StatusUpdateState.InvalidTransition]
     * so the UI can display a contextual error without string-matching on exception messages.
     */
    fun updateStatus(issueId: UUID, newStatus: IssueStatus) {
        if (_uiState.value.statusUpdateState is StatusUpdateState.Updating) return

        viewModelScope.launch {
            _uiState.update { it.copy(statusUpdateState = StatusUpdateState.Updating) }
            _uiState.update {
                it.copy(
                    statusUpdateState = try {
                        val updated = updateIssueStatusUseCase(issueId, newStatus)
                        it.issue?.let { _ -> _uiState.update { s -> s.copy(issue = updated) } }
                        StatusUpdateState.Success
                    } catch (e: AppException.InvalidStateTransitionException) {
                        StatusUpdateState.InvalidTransition(
                            e.message ?: "Chuyển trạng thái không hợp lệ."
                        )
                    } catch (e: AppException) {
                        StatusUpdateState.Error(e.message ?: "Cập nhật thất bại.")
                    }
                )
            }
        }
    }

    /** TC-C15: soft delete — caller navigates back on success. */
    fun deleteIssue(issueId: UUID) {
        viewModelScope.launch {
            try {
                softDeleteIssueUseCase(issueId)
                _uiState.update { it.copy(navigateBack = true) }
            } catch (e: AppException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearStatusState() {
        _uiState.update { it.copy(statusUpdateState = StatusUpdateState.Idle) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
