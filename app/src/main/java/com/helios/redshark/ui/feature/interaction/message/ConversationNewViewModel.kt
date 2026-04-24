package com.helios.redshark.ui.feature.interaction.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.domain.usecase.message.FindOrCreateDirectConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationNewUiState(
    val isLoading: Boolean = true,
    val conversationId: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ConversationNewViewModel @Inject constructor(
    private val findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationNewUiState())
    val uiState: StateFlow<ConversationNewUiState> = _uiState.asStateFlow()

    fun resolve(peerId: String) {
        viewModelScope.launch {
            runCatching { findOrCreateDirectConversationUseCase(peerId) }
                .onSuccess { conversation ->
                    _uiState.update {
                        it.copy(isLoading = false, conversationId = conversation.id.toString(), errorMessage = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
        }
    }
}

