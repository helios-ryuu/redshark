package com.helios.redshark.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaReaction
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.comment.GetCommentsUseCase
import com.helios.redshark.domain.usecase.idea.GetAllIdeasUseCase
import com.helios.redshark.domain.usecase.idea.GetIdeaReactionUseCase
import com.helios.redshark.domain.usecase.idea.SetIdeaReactionUseCase
import com.helios.redshark.domain.usecase.user.GetUsersUseCase
import com.helios.redshark.ui.common.ReactionUpdate
import com.helios.redshark.ui.common.applyReactionUpdate
import com.helios.redshark.ui.common.nextDownvoteUpdate
import com.helios.redshark.ui.common.nextUpvoteUpdate
import com.helios.redshark.ui.common.observedReactionState
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

data class HomeUiState(
    val ideas: List<Idea> = emptyList(),
    val commentCounts: Map<UUID, Int> = emptyMap(),
    val reactionStates: Map<UUID, IdeaReaction> = emptyMap(),
    val upvoteDeltas: Map<UUID, Int> = emptyMap(),
    val usersById: Map<String, User> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllIdeasUseCase: GetAllIdeasUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getIdeaReactionUseCase: GetIdeaReactionUseCase,
    private val setIdeaReactionUseCase: SetIdeaReactionUseCase,
    private val getUsersUseCase: GetUsersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val commentJobs = mutableMapOf<UUID, Job>()
    private val reactionJobs = mutableMapOf<UUID, Job>()
    private val lastUpvoteCounts = mutableMapOf<UUID, Int>()

    init {
        observeFeed()
    }

    private fun observeFeed() {
        viewModelScope.launch {
            getAllIdeasUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải dữ liệu.")
                    }
                }
                .collect { ideas ->
                    val changedUpvoteIds = ideas.mapNotNull { idea ->
                        val lastCount = lastUpvoteCounts[idea.id]
                        if (lastCount != null && lastCount != idea.upvoteCount) idea.id else null
                    }.toSet()
                    lastUpvoteCounts.clear()
                    ideas.forEach { idea -> lastUpvoteCounts[idea.id] = idea.upvoteCount }

                    _uiState.update { state ->
                        state.copy(
                            ideas = ideas,
                            isLoading = false,
                            errorMessage = null,
                            upvoteDeltas = if (changedUpvoteIds.isEmpty()) state.upvoteDeltas
                            else state.upvoteDeltas - changedUpvoteIds,
                        )
                    }
                    syncIdeaObservers(ideas)
                    refreshUsers(ideas.map { it.authorId } + ideas.flatMap { it.collaboratorIds })
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

    private fun syncIdeaObservers(ideas: List<Idea>) {
        val activeIds = ideas.map { it.id }.toSet()
        val removedIds = commentJobs.keys - activeIds
        if (removedIds.isNotEmpty()) {
            removedIds.forEach { id -> commentJobs.remove(id)?.cancel() }
            reactionJobs.keys.intersect(removedIds).forEach { id -> reactionJobs.remove(id)?.cancel() }
            _uiState.update { state ->
                state.copy(
                    commentCounts = state.commentCounts - removedIds,
                    reactionStates = state.reactionStates - removedIds,
                    upvoteDeltas = state.upvoteDeltas - removedIds,
                )
            }
        }
        val missingCommentIds = activeIds - commentJobs.keys
        missingCommentIds.forEach { ideaId -> observeCommentCount(ideaId) }
        val missingReactionIds = activeIds - reactionJobs.keys
        missingReactionIds.forEach { ideaId -> observeReaction(ideaId) }
    }

    private fun observeCommentCount(ideaId: UUID) {
        commentJobs[ideaId] = viewModelScope.launch {
            getCommentsUseCase(ideaId)
                .catch { }
                .collect { comments ->
                    _uiState.update { state ->
                        state.copy(commentCounts = state.commentCounts + (ideaId to comments.size))
                    }
                }
        }
    }

    private fun observeReaction(ideaId: UUID) {
        reactionJobs[ideaId] = viewModelScope.launch {
            getIdeaReactionUseCase(ideaId)
                .catch { }
                .collect { reaction ->
                    _uiState.update { state ->
                        state.copy(
                            reactionStates = observedReactionState(
                                state.reactionStates,
                                ideaId,
                                reaction,
                            )
                        )
                    }
                }
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeFeed()
    }

    fun toggleUpvote(ideaId: UUID) = toggleReaction(ideaId, ::nextUpvoteUpdate)

    fun toggleDownvote(ideaId: UUID) = toggleReaction(ideaId, ::nextDownvoteUpdate)

    private fun toggleReaction(
        ideaId: UUID,
        nextUpdate: (IdeaReaction) -> ReactionUpdate,
    ) {
        var currentReaction = IdeaReaction.NONE
        var update = ReactionUpdate(IdeaReaction.NONE, 0)
        _uiState.update { state ->
            currentReaction = state.reactionStates[ideaId] ?: IdeaReaction.NONE
            update = nextUpdate(currentReaction)
            val maps = applyReactionUpdate(
                reactionStates = state.reactionStates,
                upvoteDeltas = state.upvoteDeltas,
                ideaId = ideaId,
                update = update,
            )
            state.copy(reactionStates = maps.reactionStates, upvoteDeltas = maps.upvoteDeltas)
        }
        viewModelScope.launch {
            try {
                setIdeaReactionUseCase(ideaId, update.nextReaction)
            } catch (e: Exception) {
                val rollbackUpdate = ReactionUpdate(currentReaction, -update.deltaChange)
                _uiState.update {
                    val maps = applyReactionUpdate(
                        reactionStates = it.reactionStates,
                        upvoteDeltas = it.upvoteDeltas,
                        ideaId = ideaId,
                        update = rollbackUpdate,
                    )
                    it.copy(reactionStates = maps.reactionStates, upvoteDeltas = maps.upvoteDeltas)
                }
            }
        }
    }
}
