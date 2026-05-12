package com.helios.redshark.ui.message

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@Composable
fun ShareConversationSheetContent(
    currentUserId: String?,
    messageText: String,
    onSent: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.listState.collectAsStateWithLifecycle()
    val authRequiredMessage = stringResource(R.string.message_error_auth_required)

    Column(modifier = modifier.fillMaxWidth()) {
        when {
            currentUserId == null -> EmptyContent(
                message = authRequiredMessage,
                icon = Icons.AutoMirrored.Outlined.Chat,
            )
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> ErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::retryList,
            )
            uiState.conversations.isEmpty() -> EmptyContent(
                message = stringResource(R.string.message_share_empty),
                subtitle = stringResource(R.string.message_share_empty_subtitle),
                icon = Icons.AutoMirrored.Outlined.Chat,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = Dimens.SpaceLg,
                    end = Dimens.SpaceLg,
                    top = Dimens.SpaceSm,
                    bottom = Dimens.SpaceLg,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                items(uiState.conversations, key = { it.id.toString() }) { conversation ->
                    val peerId = conversation.participantIds.firstOrNull { it != currentUserId }
                    val peerUser = peerId?.let { uiState.usersById[it] }
                    val displayName = peerUser?.displayName ?: peerId ?: stringResource(R.string.message_unknown_user)
                    ShareConversationRow(
                        conversationId = conversation.id,
                        displayName = displayName,
                        avatarUrl = peerUser?.avatarUrl,
                        onClick = {
                            viewModel.sendMessage(conversation.id, messageText, currentUserId)
                            onSent()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareConversationRow(
    conversationId: UUID,
    displayName: String,
    avatarUrl: String?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.CardBorderWidth, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarImage(
                avatarUrl = avatarUrl,
                displayName = displayName,
                size = Dimens.AvatarMd,
            )
            Spacer(modifier = Modifier.size(Dimens.SpaceMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = conversationId.toString().take(8),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.action_send),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
