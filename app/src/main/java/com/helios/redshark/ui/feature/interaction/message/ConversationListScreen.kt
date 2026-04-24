package com.helios.redshark.ui.feature.interaction.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.User

@Composable
fun ConversationListScreen(
    modifier: Modifier = Modifier,
    onOpenConversation: (String, String?) -> Unit,
    onCreateConversation: (String) -> Unit,
    currentUserId: String?,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    val peerCandidates = uiState.users
        .filter { it.id != currentUserId }
        .filter { user ->
            uiState.conversations.none { conversation ->
                conversation.participantIds.contains(user.id)
            }
        }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        uiState.errorMessage ?: stringResource(R.string.common_error_generic),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            uiState.conversations.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        stringResource(R.string.interaction_messages_empty_conversations),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(uiState.conversations, key = { it.id }) { conversation ->
                        ConversationRow(
                            conversation = conversation,
                            users = uiState.users,
                            currentUserId = currentUserId,
                            onClick = { title ->
                                onOpenConversation(conversation.id.toString(), title)
                            },
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showCreateDialog = true },
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.interaction_messages_create_content_description),
                )
            },
            text = { Text(stringResource(R.string.interaction_messages_create)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showCreateDialog) {
        ConversationCandidateDialog(
            users = peerCandidates,
            errorMessage = uiState.createErrorMessage,
            onDismiss = { showCreateDialog = false },
            onSelect = { user ->
                showCreateDialog = false
                onCreateConversation(user.id)
            },
        )
    }
}

@Composable
private fun ConversationCandidateDialog(
    users: List<User>,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSelect: (User) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.interaction_messages_create)) },
        text = {
            when {
                errorMessage != null -> Text(errorMessage)
                users.isEmpty() -> Text(stringResource(R.string.interaction_messages_no_available_users))
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(users, key = { it.id }) { user ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(user) }
                                    .padding(vertical = 8.dp),
                            ) {
                                Text(user.displayName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.interaction_messages_close))
            }
        },
    )
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    users: List<User>,
    currentUserId: String?,
    onClick: (String) -> Unit,
) {
    val peerUser = users.firstOrNull { user ->
        user.id != currentUserId && conversation.participantIds.contains(user.id)
    }
    val title = peerUser?.displayName ?: conversation.participantIds.joinToString(separator = ", ")
    val subtitle = conversation.lastMessage
        ?: peerUser?.email
        ?: stringResource(R.string.interaction_messages_empty_messages)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(title) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

