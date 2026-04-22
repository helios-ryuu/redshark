package com.helios.redshark.ui.feature.interaction.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.MessageDeliveryStatus
import com.helios.redshark.domain.usecase.message.GetMessagesUseCase
import com.helios.redshark.domain.usecase.message.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class ConversationUiState(
    val currentUserId: String? = null,
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState(currentUserId = firebaseAuth.currentUser?.uid))
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    fun observe(conversationId: UUID) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getMessagesUseCase(conversationId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Unable to load messages",
                        )
                    }
                }
                .collect { list ->
                    _uiState.update { state ->
                        val pending = state.messages.filter { it.status != MessageDeliveryStatus.SENT }
                        state.copy(
                            messages = (list + pending).distinctBy { it.id },
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun send(conversationId: UUID) {
        val currentUserId = uiState.value.currentUserId
        if (currentUserId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "You need to sign in to send messages") }
            return
        }

        val content = uiState.value.draft.trim()
        if (content.isBlank()) return

        val pendingId = UUID.randomUUID()
        val pendingMessage = Message(
            id = pendingId,
            conversationId = conversationId,
            senderId = currentUserId,
            content = content,
            createdAt = Instant.now(),
            status = MessageDeliveryStatus.SENDING,
        )

        _uiState.update {
            it.copy(
                draft = "",
                messages = it.messages + pendingMessage,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching { sendMessageUseCase(conversationId, content) }
                .onSuccess { sent ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages
                                .map { msg -> if (msg.id == pendingId) sent else msg }
                                .distinctBy { msg -> msg.id },
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages.map { msg ->
                                if (msg.id == pendingId) msg.copy(status = MessageDeliveryStatus.FAILED) else msg
                            },
                            errorMessage = e.message ?: "Failed to send message",
                        )
                    }
                }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

