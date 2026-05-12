package com.helios.redshark.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.CreateCommentInput
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.comment.CreateCommentUseCase
import com.helios.redshark.domain.usecase.comment.GetCommentsUseCase
import com.helios.redshark.domain.usecase.user.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CommentUiState(
    val comments: List<Comment> = emptyList(),
    val usersById: Map<String, User> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val getUsersUseCase: GetUsersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun load(ideaId: UUID) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getCommentsUseCase(ideaId)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải bình luận.")
                    }
                }
                .collect { comments ->
                    _uiState.update {
                        it.copy(comments = comments, isLoading = false, errorMessage = null)
                    }
                    refreshUsers(comments.map { it.authorId })
                }
        }
    }

    private fun refreshUsers(userIds: List<String>) {
        viewModelScope.launch {
            when (val result = getUsersUseCase()) {
                is Result.Success -> {
                    val neededIds = userIds.toSet()
                    _uiState.update { state ->
                        state.copy(usersById = result.data.filter { it.id in neededIds }.associateBy { it.id })
                    }
                }
                else -> Unit
            }
        }
    }

    fun retry(ideaId: UUID) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        load(ideaId)
    }

    fun sendComment(ideaId: UUID, content: String, currentUserId: String) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            try {
                createCommentUseCase(CreateCommentInput(ideaId = ideaId, content = content), currentUserId)
                _uiState.update { it.copy(isSubmitting = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, submitError = e.message ?: "Không thể gửi bình luận.")
                }
            }
        }
    }

    fun clearSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }
}

