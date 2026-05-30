package com.helios.redshark.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.message.FindOrCreateDirectConversationUseCase
import com.helios.redshark.domain.usecase.message.GetConversationsUseCase
import com.helios.redshark.domain.usecase.message.GetMessagesUseCase
import com.helios.redshark.domain.usecase.message.SendMessageUseCase
import com.helios.redshark.domain.usecase.message.MarkConversationReadUseCase
import com.helios.redshark.domain.usecase.message.ShareMessageToRecipientsUseCase
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

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val usersById: Map<String, User> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class ConversationUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val navigateToConversation: UUID? = null,
)

data class ShareSheetUiState(
    val searchQuery: String = "",
    val selectedUserIds: Set<String> = emptySet(),
    val failedUserIds: Set<String> = emptySet(),
    val isSending: Boolean = false,
    val statusMessage: String? = null,
)

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val markConversationReadUseCase: MarkConversationReadUseCase,
    private val shareMessageToRecipientsUseCase: ShareMessageToRecipientsUseCase,
) : ViewModel() {

    private val _listState = MutableStateFlow(ConversationListUiState())
    val listState: StateFlow<ConversationListUiState> = _listState.asStateFlow()

    private val _convState = MutableStateFlow(ConversationUiState())
    val convState: StateFlow<ConversationUiState> = _convState.asStateFlow()

    private val _shareState = MutableStateFlow(ShareSheetUiState())
    val shareState: StateFlow<ShareSheetUiState> = _shareState.asStateFlow()

    init {
        observeConversations()
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            when (val result = getUsersUseCase()) {
                is com.helios.redshark.core.util.Result.Success -> {
                    val map = result.data.associateBy { it.id }
                    _listState.update { it.copy(usersById = map) }
                }
                else -> Unit
            }
        }
    }

    fun retryList() {
        _listState.update { it.copy(isLoading = true, errorMessage = null) }
        observeConversations()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            getConversationsUseCase()
                .catch { e ->
                    _listState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải tin nhắn.") }
                }
                .collect { list ->
                    _listState.update { it.copy(conversations = list, isLoading = false, errorMessage = null) }
                }
        }
    }

    fun loadMessages(conversationId: UUID) {
        viewModelScope.launch {
            _convState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { markConversationReadUseCase(conversationId) }
            getMessagesUseCase(conversationId)
                .catch { e ->
                    _convState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải tin nhắn.") }
                }
                .collect { list ->
                    _convState.update { it.copy(messages = list, isLoading = false) }
                }
        }
    }

    fun sendMessage(conversationId: UUID, content: String, currentUserId: String) {
        if (_convState.value.isSending) return
        val rollback = _convState.value.messages
        val optimistic = Message(
            id = UUID.randomUUID(),
            conversationId = conversationId,
            senderId = currentUserId,
            content = content,
            createdAt = Instant.now(),
        )
        _convState.update { it.copy(messages = it.messages + optimistic, isSending = true) }
        viewModelScope.launch {
            runCatching {
                sendMessageUseCase(SendMessageInput(conversationId, content))
            }.onFailure { e ->
                _convState.update { it.copy(messages = rollback, isSending = false, errorMessage = e.message) }
            }.onSuccess {
                _convState.update { it.copy(isSending = false) }
            }
        }
    }

    fun findOrCreateConversation(peerId: String) {
        viewModelScope.launch {
            _convState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { findOrCreateDirectConversationUseCase(peerId) }
                .onSuccess { conv ->
                    _convState.update { it.copy(isLoading = false, navigateToConversation = conv.id) }
                }
                .onFailure { e ->
                    _convState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun clearNavigation() {
        _convState.update { it.copy(navigateToConversation = null) }
    }

    fun clearError() {
        _convState.update { it.copy(errorMessage = null) }
        _listState.update { it.copy(errorMessage = null) }
    }

    fun resetShareState() {
        _shareState.value = ShareSheetUiState()
    }

    fun setShareSearchQuery(query: String) {
        _shareState.update { it.copy(searchQuery = query) }
    }

    fun toggleShareRecipient(userId: String) {
        _shareState.update { state ->
            val selected = state.selectedUserIds.toMutableSet()
            if (!selected.add(userId)) selected.remove(userId)
            state.copy(
                selectedUserIds = selected,
                failedUserIds = state.failedUserIds - userId,
                statusMessage = null,
            )
        }
    }

    fun sendSharedMessage(messageText: String, onSent: () -> Unit) {
        val recipients = _shareState.value.selectedUserIds
        if (recipients.isEmpty()) {
            _shareState.update { it.copy(statusMessage = "Chọn ít nhất một người nhận.") }
            return
        }
        if (_shareState.value.isSending) return

        viewModelScope.launch {
            _shareState.update { it.copy(isSending = true, statusMessage = null, failedUserIds = emptySet()) }
            runCatching {
                shareMessageToRecipientsUseCase(messageText, recipients)
            }.onSuccess { result ->
                if (result.isComplete) {
                    resetShareState()
                    onSent()
                } else {
                    val failedIds = result.failures.map { it.recipientUserId }.toSet()
                    val message = if (result.sentCount > 0) {
                        "Đã gửi ${result.sentCount}/${result.totalCount}. Người nhận lỗi vẫn được chọn."
                    } else {
                        "Không gửi được. Vui lòng thử lại."
                    }
                    _shareState.update {
                        it.copy(
                            selectedUserIds = failedIds,
                            failedUserIds = failedIds,
                            isSending = false,
                            statusMessage = message,
                        )
                    }
                }
            }.onFailure { error ->
                _shareState.update {
                    it.copy(
                        isSending = false,
                        statusMessage = error.message ?: "Không gửi được. Vui lòng thử lại.",
                    )
                }
            }
        }
    }
}
