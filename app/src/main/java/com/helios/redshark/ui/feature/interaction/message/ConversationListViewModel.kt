package com.helios.redshark.ui.feature.interaction.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.usecase.message.GetConversationsUseCase
import com.helios.redshark.domain.usecase.user.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val users: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val createErrorMessage: String? = null,
)

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    getConversationsUseCase: GetConversationsUseCase,
    getUsersUseCase: GetUsersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        observeUsers(getUsersUseCase)
        viewModelScope.launch {
            getConversationsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Unable to load conversations")
                    }
                }
                .collect { list ->
                    _uiState.update { it.copy(conversations = list, isLoading = false, errorMessage = null) }
                }
        }
    }

    private fun observeUsers(getUsersUseCase: GetUsersUseCase) {
        viewModelScope.launch {
            when (val result = getUsersUseCase()) {
                is Result.Success -> _uiState.update { it.copy(users = result.data) }
                is Result.Error -> _uiState.update { it.copy(createErrorMessage = result.exception.message) }
                Result.Loading -> Unit
            }
        }
    }
}

