package com.helios.redshark.ui.createissue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateIssueInput
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.domain.usecase.issue.CreateIssueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Represents every distinct UI outcome so the screen reacts without any string matching. */
sealed interface CreateIssueUiState {
    data object Idle : CreateIssueUiState
    data object Loading : CreateIssueUiState
    data class Success(val issueId: UUID) : CreateIssueUiState

    sealed interface Failure : CreateIssueUiState {
        /** User already has 20 active issues — show "Đạt giới hạn 20 issue" toast. */
        data object LimitExceeded : Failure
        /** TC-C08: parent idea is CLOSED or CANCELLED — cannot add new issues. */
        data object IdeaNotActive : Failure
        /** A field failed domain validation — show inline error next to the field. */
        data class ValidationError(val message: String) : Failure
        /** Network / remote / unknown error — show generic snackbar. */
        data class GenericError(val message: String) : Failure
    }
}

@HiltViewModel
class CreateIssueViewModel @Inject constructor(
    private val createIssueUseCase: CreateIssueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateIssueUiState>(CreateIssueUiState.Idle)
    val uiState: StateFlow<CreateIssueUiState> = _uiState.asStateFlow()

    /**
     * Entry point called by the screen on form submit.
     *
     * @param currentUserId Firebase UID from the auth session — supplied by the caller
     *   so this ViewModel stays independent of the auth layer.
     */
    fun createIssue(
        ideaId: UUID,
        title: String,
        description: String?,
        priority: IssuePriority,
        currentUserId: String
    ) {
        if (_uiState.value is CreateIssueUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = CreateIssueUiState.Loading

            _uiState.value = try {
                val issue = createIssueUseCase(
                    input = CreateIssueInput(
                        ideaId = ideaId,
                        title = title,
                        description = description,
                        priority = priority
                    ),
                    currentUserId = currentUserId
                )
                CreateIssueUiState.Success(issue.id)

            } catch (e: AppException.IssueLimitExceededException) {
                CreateIssueUiState.Failure.LimitExceeded

            } catch (e: AppException.IdeaNotActiveException) {
                // TC-C08: idea is CLOSED/CANCELLED — distinct state so UI can show correct message
                CreateIssueUiState.Failure.IdeaNotActive

            } catch (e: AppException.ValidationException) {
                CreateIssueUiState.Failure.ValidationError(e.message ?: "Dữ liệu không hợp lệ.")

            } catch (e: AppException) {
                CreateIssueUiState.Failure.GenericError(e.message ?: "Đã xảy ra lỗi. Vui lòng thử lại.")
            }
        }
    }

    /** Reset to Idle after the screen has consumed a terminal state (Success / Failure). */
    fun resetState() {
        _uiState.value = CreateIssueUiState.Idle
    }
}
