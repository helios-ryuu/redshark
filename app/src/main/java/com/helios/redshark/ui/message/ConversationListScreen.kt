package com.helios.redshark.ui.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun ConversationListScreen(
    currentUserId: String?,
    onOpenConversation: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.listState.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> ErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::retryList,
            )
            uiState.conversations.isEmpty() -> EmptyContent(message = stringResource(R.string.message_list_empty))
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                items(uiState.conversations, key = { it.id.toString() }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        currentUserId = currentUserId,
                        onClick = { onOpenConversation(conv.id) },
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peerId.take(8) + "...",
                    style = MaterialTheme.typography.titleSmall,
                )
                conversation.lastMessagePreview?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            conversation.lastMessageAt?.let { time ->
                Text(
                    text = time.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
