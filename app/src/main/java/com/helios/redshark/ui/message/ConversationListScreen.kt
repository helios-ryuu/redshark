package com.helios.redshark.ui.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import com.helios.redshark.R
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@Composable
fun ConversationListScreen(
    currentUserId: String?,
    onOpenConversation: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.listState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> ErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::retryList,
            )
            uiState.conversations.isEmpty() -> EmptyContent(
                message = stringResource(R.string.message_list_empty),
                subtitle = stringResource(R.string.message_list_empty_subtitle),
                icon = Icons.AutoMirrored.Outlined.Chat,
            )
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.conversations, key = { it.id.toString() }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        currentUserId = currentUserId,
                        onClick = { onOpenConversation(conv.id) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpaceLg))
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    currentUserId: String?,
    onClick: () -> Unit,
) {
    val unknownLabel = stringResource(R.string.message_unknown_user)
    val peerId = conversation.participantIds.firstOrNull { it != currentUserId } ?: unknownLabel
    val isSentByMe = conversation.lastMessageSenderId == currentUserId
    val showUnread = conversation.hasUnread && !isSentByMe

    val previewText = when {
        conversation.lastMessagePreview == null -> ""
        isSentByMe -> stringResource(R.string.message_preview_you_prefix, conversation.lastMessagePreview)
        else -> conversation.lastMessagePreview
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarImage(
            avatarUrl = null,
            displayName = peerId,
            size = Dimens.AvatarMd,
        )
        Spacer(modifier = Modifier.width(Dimens.SpaceMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peerId.take(8),
                style = MaterialTheme.typography.titleSmall,
            )
            if (previewText.isNotEmpty()) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall.let {
                        if (showUnread) it.copy(fontWeight = FontWeight.Bold) else it
                    },
                    color = if (showUnread)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
