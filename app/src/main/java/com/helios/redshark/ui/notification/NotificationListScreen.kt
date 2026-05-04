package com.helios.redshark.ui.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
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
    onOpenIdea: (java.util.UUID) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleNotifications = remember(uiState.notifications) {
        uiState.notifications.filter { !it.isRead }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> ErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::retry,
            )
            visibleNotifications.isEmpty() -> EmptyContent(
                message = stringResource(R.string.notification_empty),
                subtitle = stringResource(R.string.notification_empty_subtitle),
                icon = Icons.Outlined.NotificationsNone,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                items(visibleNotifications, key = { it.id.toString() }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onRead = {
                            viewModel.markAsRead(notification)
                            if (notification.targetType == NotificationTargetType.IDEA &&
                                notification.type in setOf(
                                    NotificationType.COLLAB_ACCEPTED,
                                    NotificationType.COLLAB_REJECTED,
                                )
                            ) {
                                onOpenIdea(notification.targetId)
                            }
                        },
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
    val isUnread = !notification.isRead

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRead() },
        shape = MaterialTheme.shapes.medium,
        color = if (isUnread)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = Dimens.CardBorderWidth,
            color = if (isUnread)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val typeDotColor = when (notification.type) {
                        NotificationType.COLLAB_REQUEST,
                        NotificationType.COLLAB_ACCEPTED,
                        NotificationType.COLLAB_REJECTED -> MaterialTheme.colorScheme.tertiary
                        NotificationType.COMMENT          -> MaterialTheme.colorScheme.primary
                        NotificationType.ISSUE_CREATED    -> MaterialTheme.colorScheme.secondary
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.SpaceSm)
                                .clip(CircleShape)
                                .background(typeDotColor),
                        )
                        Text(
                            text = stringResource(notification.type.labelRes()),
                            style = MaterialTheme.typography.labelSmall,
                            color = typeDotColor,
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (isUnread) {
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Box(
                        modifier = Modifier
                            .size(Dimens.UnreadDotSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
            Text(
                text = notification.createdAt.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (notification.type == NotificationType.COLLAB_REQUEST && isUnread) {
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
