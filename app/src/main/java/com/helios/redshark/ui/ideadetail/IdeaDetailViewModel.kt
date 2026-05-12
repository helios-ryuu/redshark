package com.helios.redshark.ui.ideadetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.CreateCommentInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.comment.CreateCommentUseCase
import com.helios.redshark.domain.usecase.comment.GetCommentsUseCase
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.usecase.idea.GetIdeaDetailUseCase
import com.helios.redshark.domain.usecase.idea.SoftDeleteIdeaUseCase
import com.helios.redshark.domain.usecase.idea.UpdateIdeaStatusUseCase
import com.helios.redshark.domain.usecase.idea.UpdateIdeaMediaUseCase
import com.helios.redshark.domain.usecase.idea.UploadIdeaMediaUseCase
import com.helios.redshark.domain.usecase.issue.GetIssuesByIdeaUseCase
import com.helios.redshark.domain.usecase.notification.RequestCollabUseCase
import com.helios.redshark.domain.usecase.user.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class IdeaDetailUiState(
    val idea: Idea? = null,
    val issues: List<Issue> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val usersById: Map<String, User> = emptyMap(),
    val isLoadingIdea: Boolean = false,
    /** TC-C06: true when the logged-in user is the idea author → UI shows Edit/Delete buttons. */
    val isCurrentUserAuthor: Boolean = false,
    val commentSubmitState: CommentSubmitState = CommentSubmitState.Idle,
    val statusUpdateError: String? = null,
    val errorMessage: String? = null,
    val collabRequestState: CollabRequestState = CollabRequestState.Idle,
    val isUploadingMedia: Boolean = false,
    /** TC-C22: true while a retry attempt is in flight. */
    val isRetrying: Boolean = false,
    val navigateBack: Boolean = false,
)

sealed interface CommentSubmitState {
    data object Idle : CommentSubmitState
    data object Submitting : CommentSubmitState
    data object Success : CommentSubmitState
    data class Error(val message: String) : CommentSubmitState
}

sealed interface CollabRequestState {
    data object Idle : CollabRequestState
    data object Sending : CollabRequestState
    data object Sent : CollabRequestState
    data class Error(val message: String) : CollabRequestState
}

@HiltViewModel
class IdeaDetailViewModel @Inject constructor(
    private val getIdeaDetailUseCase: GetIdeaDetailUseCase,
    private val getIssuesByIdeaUseCase: GetIssuesByIdeaUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val softDeleteIdeaUseCase: SoftDeleteIdeaUseCase,
    private val updateIdeaStatusUseCase: UpdateIdeaStatusUseCase,
    private val requestCollabUseCase: RequestCollabUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val uploadIdeaMediaUseCase: UploadIdeaMediaUseCase,
    private val updateIdeaMediaUseCase: UpdateIdeaMediaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeaDetailUiState())
    val uiState: StateFlow<IdeaDetailUiState> = _uiState.asStateFlow()

    fun loadIdea(ideaId: UUID, currentUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingIdea = true, errorMessage = null) }
            try {
                val idea = getIdeaDetailUseCase(ideaId)
                _uiState.update {
                    it.copy(
                        idea = idea,
                        isLoadingIdea = false,
                        isCurrentUserAuthor = idea.authorId == currentUserId,
                        collabRequestState = CollabRequestState.Idle,
                    )
                }
                refreshUsers(listOf(idea.authorId) + idea.collaboratorIds)
            } catch (e: AppException) {
                _uiState.update { it.copy(isLoadingIdea = false, errorMessage = e.message) }
            }
        }
        observeComments(ideaId)
        observeIssues(ideaId)
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

    private fun observeIssues(ideaId: UUID) {
        viewModelScope.launch {
            getIssuesByIdeaUseCase(ideaId)
                .catch { _uiState.update { it.copy(errorMessage = "Không thể tải danh sách issue.") } }
                .collect { list -> _uiState.update { it.copy(issues = list) } }
        }
    }

    /** TC-C22: re-fetch after network failure without losing the ideaId. */
    fun retryLoad(ideaId: UUID, currentUserId: String) {
        _uiState.update { it.copy(isRetrying = true, errorMessage = null) }
        loadIdea(ideaId, currentUserId)
        _uiState.update { it.copy(isRetrying = false) }
    }

    private fun observeComments(ideaId: UUID) {
        viewModelScope.launch {
            getCommentsUseCase(ideaId)
                .catch { _uiState.update { it.copy(errorMessage = "Không thể tải bình luận.") } }
                .collect { serverComments ->
                    // Server emission replaces the list, which also clears any optimistic items
                    _uiState.update { it.copy(comments = serverComments) }
                    refreshUsers(serverComments.map { it.authorId } + (_uiState.value.idea?.authorId ?: ""))
                }
        }
    }

    fun uploadMedia(context: Context, ideaId: UUID, currentUserId: String, uri: Uri) {
        val idea = _uiState.value.idea ?: return
        if (currentUserId != idea.authorId && currentUserId !in idea.collaboratorIds) {
            _uiState.update { it.copy(errorMessage = "Bạn cần là tác giả hoặc cộng tác viên để tải media.") }
            return
        }
        if (_uiState.value.isUploadingMedia) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingMedia = true, errorMessage = null) }
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "application/octet-stream"
                val fileName = uri.lastPathSegment?.substringAfterLast('/')
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw AppException.ValidationException("media", "Không thể đọc tệp media.")
                when (val upload = uploadIdeaMediaUseCase(ideaId, currentUserId, bytes, mimeType, fileName)) {
                    is Result.Success -> {
                        val updated = updateIdeaMediaUseCase(
                            ideaId,
                            idea.mediaAttachments + upload.data,
                        )
                        _uiState.update { it.copy(idea = updated, isUploadingMedia = false) }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isUploadingMedia = false, errorMessage = upload.exception.message)
                    }
                    is Result.Loading -> Unit
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingMedia = false, errorMessage = e.message ?: "Tải media thất bại.") }
            }
        }
    }

    /**
     * Sends a comment using an optimistic update pattern:
     *
     * 1. A temporary [Comment] is appended immediately so the UI feels instant.
     * 2. The use case is called; if it succeeds, the live [observeComments] Flow
     *    replaces the list with the server-confirmed version automatically.
     * 3. On failure, the list is rolled back to the snapshot taken before step 1,
     *    and [CommentSubmitState.Error] is exposed so the UI can prompt a retry.
     */
    fun sendComment(ideaId: UUID, content: String, currentUserId: String) {
        if (_uiState.value.commentSubmitState is CommentSubmitState.Submitting) return

        // Capture current list as the rollback point before mutating state
        val rollbackSnapshot = _uiState.value.comments
        val optimisticComment = buildOptimisticComment(ideaId, content, currentUserId)

        _uiState.update { state ->
            state.copy(
                comments = state.comments + optimisticComment,
                commentSubmitState = CommentSubmitState.Submitting
            )
        }

        viewModelScope.launch {
            try {
                createCommentUseCase(
                    input = CreateCommentInput(ideaId, content),
                    currentUserId = currentUserId
                )
                // Success: observeComments will deliver the confirmed list; just clear submit state
                _uiState.update { it.copy(commentSubmitState = CommentSubmitState.Success) }

            } catch (e: AppException) {
                // Rollback: restore the pre-optimistic snapshot so no ghost comment lingers
                _uiState.update { state ->
                    state.copy(
                        comments = rollbackSnapshot,
                        commentSubmitState = CommentSubmitState.Error(
                            e.message ?: "Gửi bình luận thất bại. Vui lòng thử lại."
                        )
                    )
                }
            }
        }
    }

    /**
     * Builds a temporary placeholder [Comment] for immediate display.
     * The random UUID is intentionally discarded once the server response arrives
     * and [observeComments] replaces the list with the real record.
     */
    private fun buildOptimisticComment(ideaId: UUID, content: String, authorId: String) = Comment(
        id = UUID.randomUUID(),
        ideaId = ideaId,
        authorId = authorId,
        content = content,
        createdAt = Instant.now()
    )

    fun changeStatus(ideaId: UUID, newStatus: IdeaStatus) {
        viewModelScope.launch {
            try {
                val updated = updateIdeaStatusUseCase(ideaId, newStatus)
                _uiState.update { it.copy(idea = updated, statusUpdateError = null) }
            } catch (e: AppException) {
                _uiState.update { it.copy(statusUpdateError = e.message) }
            }
        }
    }

    fun deleteIdea(ideaId: UUID) {
        viewModelScope.launch {
            try {
                softDeleteIdeaUseCase(ideaId)
                _uiState.update { it.copy(navigateBack = true) }
            } catch (e: AppException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun requestCollab(ideaId: UUID) {
        if (_uiState.value.collabRequestState is CollabRequestState.Sending) return
        _uiState.update { it.copy(collabRequestState = CollabRequestState.Sending) }
        viewModelScope.launch {
            try {
                requestCollabUseCase(ideaId)
                _uiState.update { it.copy(collabRequestState = CollabRequestState.Sent) }
            } catch (e: AppException) {
                _uiState.update { it.copy(collabRequestState = CollabRequestState.Error(e.message ?: "Gửi yêu cầu thất bại.")) }
            }
        }
    }

    fun clearCommentState() {
        _uiState.update { it.copy(commentSubmitState = CommentSubmitState.Idle) }
    }

    fun clearCollabRequestState() {
        _uiState.update { it.copy(collabRequestState = CollabRequestState.Idle) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
