package com.helios.redshark.ui.feature.interaction.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.usecase.notification.AcceptCollabUseCase
import com.helios.redshark.domain.usecase.notification.GetNotificationsUseCase
import com.helios.redshark.domain.usecase.notification.GetUnreadNotificationCountUseCase
import com.helios.redshark.domain.usecase.notification.MarkNotificationReadUseCase
import com.helios.redshark.domain.usecase.notification.RejectCollabUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationListUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val pendingConversationId: String? = null,
)

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    getNotificationsUseCase: GetNotificationsUseCase,
    getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val acceptCollabUseCase: AcceptCollabUseCase,
    private val rejectCollabUseCase: RejectCollabUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationListUiState())
    val uiState: StateFlow<NotificationListUiState> = _uiState.asStateFlow()

    init {
        observeNotifications(getNotificationsUseCase)
        observeUnreadCount(getUnreadNotificationCountUseCase)
    }

    fun markAsRead(notification: Notification) {
        if (notification.isRead) return
        launchSafe { markNotificationReadUseCase(notification.id) }
    }

    fun accept(notification: Notification) {
        if (notification.type != NotificationType.COLLAB_REQUEST) return
        launchSafe {
            val conversation = acceptCollabUseCase(notification)
            _uiState.update { it.copy(pendingConversationId = conversation.id.toString()) }
        }
    }

    fun reject(notification: Notification) {
        if (notification.type != NotificationType.COLLAB_REQUEST) return
        launchSafe { rejectCollabUseCase(notification) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onPendingConversationHandled() {
        _uiState.update { it.copy(pendingConversationId = null) }
    }

    private fun observeNotifications(getNotificationsUseCase: GetNotificationsUseCase) {
        viewModelScope.launch {
            getNotificationsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Unable to load notifications")
                    }
                }
                .collect { list ->
                    _uiState.update { it.copy(notifications = list, isLoading = false, errorMessage = null) }
                }
        }
    }

    private fun observeUnreadCount(getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase) {
        viewModelScope.launch {
            getUnreadNotificationCountUseCase()
                .catch { _uiState.update { it.copy(unreadCount = 0) } }
                .collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
        }
    }

    private fun launchSafe(action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }
}

