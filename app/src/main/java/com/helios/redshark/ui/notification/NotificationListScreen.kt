package com.helios.redshark.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun NotificationListScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> ErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::retry,
            )
            uiState.notifications.isEmpty() -> EmptyContent(message = stringResource(R.string.notification_empty))
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                items(uiState.notifications, key = { it.id.toString() }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onRead = { viewModel.markAsRead(notification) },
                        onAccept = { viewModel.acceptCollab(notification) },
                        onReject = { viewModel.rejectCollab(notification) },
                    )
                }
            }
        }

        uiState.actionError?.let {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(Dimens.SpaceLg),
                action = {
                    TextButton(onClick = viewModel::clearActionError) { Text(stringResource(R.string.action_ok)) }
                },
            ) { Text(it) }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onRead: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    val bgColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onRead() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 0.dp else Dimens.CardElevation,
        ),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                if (!notification.isRead) {
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.SpaceSm),
                    ) {}
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceXs))
            Text(
                text = stringResource(notification.type.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = notification.createdAt.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (notification.type == NotificationType.COLLAB_REQUEST && !notification.isRead) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    Button(onClick = onAccept) { Text(stringResource(R.string.notification_action_accept)) }
                    OutlinedButton(onClick = onReject) { Text(stringResource(R.string.notification_action_reject)) }
                }
            }
        }
    }
}

private fun NotificationType.labelRes(): Int = when (this) {
    NotificationType.ISSUE_CREATED -> R.string.notification_type_issue_created
    NotificationType.COLLAB_REQUEST -> R.string.notification_type_collab_request
    NotificationType.COLLAB_ACCEPTED -> R.string.notification_type_collab_accepted
    NotificationType.COLLAB_REJECTED -> R.string.notification_type_collab_rejected
    NotificationType.COMMENT -> R.string.notification_type_comment
}
