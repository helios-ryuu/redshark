package com.helios.redshark.ui.notification

// File nay xu ly trang thai va giao dien nguoi dung cho mot tinh nang.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.usecase.idea.AddSelfAsCollaboratorUseCase
import com.helios.redshark.domain.usecase.notification.AcceptCollabUseCase
import com.helios.redshark.domain.usecase.notification.DeleteAllNotificationsUseCase
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

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionError: String? = null,
)

// Quan ly state va goi use case de man hinh chi can render du lieu.
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeNotifications()
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    private fun observeUnreadCount() {
        viewModelScope.launch {
            getUnreadNotificationCountUseCase()
                .catch { }
                .collect { count -> _uiState.update { it.copy(unreadCount = count) } }
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun deleteAll() {
        viewModelScope.launch {
            runCatching { deleteAllNotificationsUseCase() }
                .onSuccess {
                    _uiState.update { it.copy(notifications = emptyList(), unreadCount = 0) }
                }
                .onFailure { e -> _uiState.update { it.copy(actionError = e.message) } }
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }
}
