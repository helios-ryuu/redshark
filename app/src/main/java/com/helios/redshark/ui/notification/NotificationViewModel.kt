package com.helios.redshark.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.usecase.idea.AddSelfAsCollaboratorUseCase
import com.helios.redshark.domain.usecase.notification.AcceptCollabUseCase
import com.helios.redshark.domain.usecase.notification.DeleteAllNotificationsUseCase
import com.helios.redshark.domain.usecase.notification.GetNotificationsUseCase
import com.helios.redshark.domain.usecase.notification.GetUnreadCountUseCase
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

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionError: String? = null,
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val getUnreadCountUseCase: GetUnreadCountUseCase,
    private val addSelfAsCollaboratorUseCase: AddSelfAsCollaboratorUseCase,
    private val acceptCollabUseCase: AcceptCollabUseCase,
    private val rejectCollabUseCase: RejectCollabUseCase,
    private val deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
        observeUnreadCount()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải thông báo.") }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            notifications = list,
                            unreadCount = list.count { notification -> !notification.isRead },
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            getUnreadCountUseCase()
                .catch { }
                .collect { count -> _uiState.update { it.copy(unreadCount = count) } }
        }
    }

    fun markAsRead(notification: Notification) {
        if (notification.isRead) return
        viewModelScope.launch {
            val markResult = if (notification.type == NotificationType.COLLAB_ACCEPTED) {
                runCatching {
                    addSelfAsCollaboratorUseCase(notification.targetId)
                    markNotificationReadUseCase(notification.id)
                }
            } else {
                runCatching { markNotificationReadUseCase(notification.id) }
            }
            markResult
                .onSuccess {
                    _uiState.update { state ->
                        val updated = state.notifications.map {
                            if (it.id == notification.id) it.copy(isRead = true) else it
                        }
                        state.copy(
                            notifications = updated,
                            unreadCount = updated.count { item -> !item.isRead },
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(actionError = e.message) } }
        }
    }

    fun acceptCollab(notification: Notification) {
        if (notification.type != NotificationType.COLLAB_REQUEST) return
        viewModelScope.launch {
            runCatching { acceptCollabUseCase(notification) }
                .onSuccess {
                    _uiState.update { state ->
                        val updated = state.notifications.map {
                            if (it.id == notification.id) it.copy(isRead = true) else it
                        }
                        state.copy(
                            notifications = updated,
                            unreadCount = updated.count { item -> !item.isRead },
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(actionError = e.message) } }
        }
    }

    fun rejectCollab(notification: Notification) {
        if (notification.type != NotificationType.COLLAB_REQUEST) return
        viewModelScope.launch {
            runCatching { rejectCollabUseCase(notification) }
                .onSuccess {
                    _uiState.update { state ->
                        val updated = state.notifications.map {
                            if (it.id == notification.id) it.copy(isRead = true) else it
                        }
                        state.copy(
                            notifications = updated,
                            unreadCount = updated.count { item -> !item.isRead },
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(actionError = e.message) } }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            runCatching { deleteAllNotificationsUseCase() }
                .onSuccess {
                    _uiState.update { it.copy(notifications = emptyList(), unreadCount = 0) }
                }
                .onFailure { e -> _uiState.update { it.copy(actionError = e.message) } }
        }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }
}
