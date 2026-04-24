package com.helios.redshark.ui.feature.interaction.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationType

@Composable
fun NotificationListScreen(
    modifier: Modifier = Modifier,
    onOpenConversation: (String) -> Unit = {},
    viewModel: NotificationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            viewModel.onErrorShown()
        }
    }

    LaunchedEffect(uiState.pendingConversationId) {
        val conversationId = uiState.pendingConversationId ?: return@LaunchedEffect
        onOpenConversation(conversationId)
        viewModel.onPendingConversationHandled()
    }

    when {
        uiState.isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.notifications.isEmpty() -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.interaction_notifications_empty),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkRead = { viewModel.markAsRead(notification) },
                        onAccept = { viewModel.accept(notification) },
                        onReject = { viewModel.reject(notification) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onMarkRead: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(notification.message, style = MaterialTheme.typography.bodyLarge)
            if (!notification.isRead) {
                OutlinedButton(onClick = onMarkRead) {
                    Text(stringResource(R.string.interaction_notifications_mark_read))
                }
            }
            if (notification.type == NotificationType.COLLAB_REQUEST) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept) { Text(stringResource(R.string.interaction_notifications_accept)) }
                    OutlinedButton(onClick = onReject) { Text(stringResource(R.string.interaction_notifications_reject)) }
                }
            }
        }
    }
}

