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

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val markConversationReadUseCase: MarkConversationReadUseCase,
) : ViewModel() {

    private val _listState = MutableStateFlow(ConversationListUiState())
    val listState: StateFlow<ConversationListUiState> = _listState.asStateFlow()

    private val _convState = MutableStateFlow(ConversationUiState())
    val convState: StateFlow<ConversationUiState> = _convState.asStateFlow()

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
}
