package com.helios.redshark.ui.message

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.User
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
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val shareState by viewModel.shareState.collectAsStateWithLifecycle()
    val authRequiredMessage = stringResource(R.string.message_error_auth_required)

    LaunchedEffect(messageText) {
        viewModel.resetShareState()
    }

    Column(modifier = modifier.fillMaxSize()) {
        when {
            currentUserId == null -> EmptyContent(
                message = authRequiredMessage,
                icon = Icons.AutoMirrored.Outlined.Chat,
                modifier = Modifier.weight(1f),
            )
            listState.isLoading && listState.usersById.isEmpty() -> LoadingContent(modifier = Modifier.weight(1f))
            listState.errorMessage != null && listState.usersById.isEmpty() -> listState.errorMessage?.let { message ->
                ErrorContent(message = message, onRetry = viewModel::retryList, modifier = Modifier.weight(1f))
            }
            else -> {
                val targets = remember(listState.conversations, listState.usersById, currentUserId) {
                    buildShareTargets(
                        conversations = listState.conversations,
                        usersById = listState.usersById,
                        currentUserId = currentUserId,
                    )
                }
                val filteredTargets = remember(targets, shareState.searchQuery) {
                    val query = shareState.searchQuery.trim().lowercase()
                    if (query.isEmpty()) {
                        targets
                    } else {
                        targets.filter { it.matches(query) }
                    }
                }

                TextField(
                    value = shareState.searchQuery,
                    onValueChange = viewModel::setShareSearchQuery,
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.message_share_search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.SearchBarMinHeight)
                        .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
                )

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        targets.isEmpty() -> EmptyContent(
                            message = stringResource(R.string.message_share_empty),
                            subtitle = stringResource(R.string.message_share_empty_subtitle),
                            icon = Icons.AutoMirrored.Outlined.Chat,
                        )
                        filteredTargets.isEmpty() -> EmptyContent(
                            message = stringResource(R.string.message_share_search_empty),
                            subtitle = stringResource(R.string.message_share_search_empty_subtitle),
                            icon = Icons.AutoMirrored.Outlined.Chat,
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = Dimens.SpaceLg,
                                end = Dimens.SpaceLg,
                                top = Dimens.SpaceSm,
                                bottom = Dimens.SpaceLg,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                        ) {
                            items(filteredTargets, key = { it.userId }) { target ->
                                ShareTargetRow(
                                    target = target,
                                    isSelected = target.userId in shareState.selectedUserIds,
                                    isFailed = target.userId in shareState.failedUserIds,
                                    onToggle = { viewModel.toggleShareRecipient(target.userId) },
                                )
                            }
                        }
                    }
                }

                shareState.statusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (shareState.failedUserIds.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceXs),
                    )
                }

                HorizontalDivider()
                Button(
                    onClick = { viewModel.sendSharedMessage(messageText, onSent) },
                    enabled = shareState.selectedUserIds.isNotEmpty() && !shareState.isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceLg),
                ) {
                    if (shareState.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.ButtonProgressIndicatorSize),
                            strokeWidth = Dimens.ButtonProgressIndicatorStroke,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(R.string.message_share_send_count, shareState.selectedUserIds.size))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareTargetRow(
    target: ShareTarget,
    isSelected: Boolean,
    isFailed: Boolean,
    onToggle: () -> Unit,
) {
    val helperText = if (target.hasConversation) {
        stringResource(R.string.message_share_existing_conversation)
    } else {
        stringResource(R.string.message_share_new_conversation)
    }
    val borderColor = when {
        isFailed -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(Dimens.CardBorderWidth, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarImage(
                avatarUrl = target.avatarUrl,
                displayName = target.displayName,
                size = Dimens.AvatarMd,
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
                Text(
                    text = target.email.ifBlank { helperText },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                if (target.email.isNotBlank()) {
                    Text(
                        text = helperText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
            )
        }
    }
}

private data class ShareTarget(
    val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val conversationId: UUID?,
) {
    val hasConversation: Boolean = conversationId != null

    fun matches(query: String): Boolean =
        displayName.lowercase().contains(query)
            || email.lowercase().contains(query)
            || userId.lowercase().contains(query)
}

private fun buildShareTargets(
    conversations: List<Conversation>,
    usersById: Map<String, User>,
    currentUserId: String,
): List<ShareTarget> {
    val conversationByPeer = conversations.mapNotNull { conversation ->
        val peerId = conversation.participantIds.firstOrNull { it != currentUserId } ?: return@mapNotNull null
        peerId to conversation
    }.toMap()

    val userTargets = usersById.values
        .filter { it.id != currentUserId }
        .map { user ->
            ShareTarget(
                userId = user.id,
                displayName = user.displayName.ifBlank { user.email.ifBlank { user.id.take(8) } },
                email = user.email,
                avatarUrl = user.avatarUrl,
                conversationId = conversationByPeer[user.id]?.id,
            )
        }

    val fallbackTargets = conversationByPeer
        .filterKeys { it !in usersById && it != currentUserId }
        .map { (peerId, conversation) ->
            ShareTarget(
                userId = peerId,
                displayName = peerId.take(8),
                email = "",
                avatarUrl = null,
                conversationId = conversation.id,
            )
        }

    return (userTargets + fallbackTargets)
        .distinctBy { it.userId }
        .sortedWith(compareByDescending<ShareTarget> { it.hasConversation }.thenBy { it.displayName.lowercase() })
}
