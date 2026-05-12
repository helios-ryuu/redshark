package com.helios.redshark.ui.message

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Patterns
import com.helios.redshark.R
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Composable
fun ConversationListScreen(
    currentUserId: String?,
    onOpenConversation: (UUID) -> Unit,
    onStartConversation: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.listState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val emailRequiredMessage = stringResource(R.string.message_error_email_required)
    val emailInvalidMessage = stringResource(R.string.message_error_email_invalid)
    val userNotFoundMessage = stringResource(R.string.message_error_user_not_found)
    val selfConversationMessage = stringResource(R.string.message_error_self_conversation)
    val authRequiredMessage = stringResource(R.string.message_error_auth_required)

    val filteredConversations = remember(uiState.conversations, uiState.usersById, searchQuery, currentUserId) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            uiState.conversations
        } else {
            uiState.conversations.filter { conv ->
                val peerId = conv.participantIds.firstOrNull { it != currentUserId } ?: return@filter false
                val peerUser = uiState.usersById[peerId]
                val displayName = peerUser?.displayName ?: ""
                val email = peerUser?.email ?: ""
                displayName.lowercase().contains(query)
                    || email.lowercase().contains(query)
                    || peerId.lowercase().contains(query)
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.message_new_conversation_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    TextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            emailError = null
                        },
                        singleLine = true,
                        label = { Text(stringResource(R.string.message_email_label)) },
                        placeholder = { Text(stringResource(R.string.message_email_placeholder)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    emailError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = emailInput.trim()
                        when {
                            trimmed.isEmpty() -> {
                                emailError = emailRequiredMessage
                            }
                            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> {
                                emailError = emailInvalidMessage
                            }
                            currentUserId == null -> {
                                emailError = authRequiredMessage
                            }
                            else -> {
                                val match = uiState.usersById.values.firstOrNull {
                                    it.email.equals(trimmed, ignoreCase = true)
                                }
                                when {
                                    match == null -> emailError = userNotFoundMessage
                                    match.id == currentUserId -> emailError = selfConversationMessage
                                    else -> {
                                        showCreateDialog = false
                                        emailInput = ""
                                        emailError = null
                                        onStartConversation(match.id)
                                    }
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.message_action_start_conversation))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.message_search_placeholder)) },
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
                uiState.isLoading -> LoadingContent()
                uiState.errorMessage != null -> uiState.errorMessage?.let { message ->
                    ErrorContent(message = message, onRetry = viewModel::retryList)
                }
                uiState.conversations.isEmpty() -> EmptyContent(
                    message = stringResource(R.string.message_list_empty),
                    subtitle = stringResource(R.string.message_list_empty_subtitle),
                    icon = Icons.AutoMirrored.Outlined.Chat,
                )
                filteredConversations.isEmpty() -> EmptyContent(
                    message = stringResource(R.string.message_search_empty),
                    subtitle = stringResource(R.string.message_search_empty_subtitle),
                    icon = Icons.AutoMirrored.Outlined.Chat,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.SpaceLg, end = Dimens.SpaceLg,
                        top = Dimens.SpaceSm, bottom = Dimens.ListBottomPaddingWithFab,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    items(filteredConversations, key = { it.id.toString() }) { conv ->
                        val peerId = conv.participantIds.firstOrNull { it != currentUserId }
                        val peerUser = peerId?.let { uiState.usersById[it] }
                        ConversationItem(
                            conversation = conv,
                            currentUserId = currentUserId,
                            displayName = peerUser?.displayName,
                            avatarUrl = peerUser?.avatarUrl,
                            onClick = { onOpenConversation(conv.id) },
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    showCreateDialog = true
                    emailInput = ""
                    emailError = null
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.SpaceLg),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.message_action_new_conversation),
                )
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    currentUserId: String?,
    displayName: String?,
    avatarUrl: String?,
    onClick: () -> Unit,
) {
    val unknownLabel = stringResource(R.string.message_unknown_user)
    val peerId = conversation.participantIds.firstOrNull { it != currentUserId } ?: unknownLabel
    val peerLabel = displayName ?: peerId
    val isSentByMe = conversation.lastMessageSenderId == currentUserId
    val showUnread = conversation.hasUnread && !isSentByMe

    val previewText = when {
        conversation.lastMessagePreview == null -> ""
        isSentByMe -> stringResource(R.string.message_preview_you_prefix, conversation.lastMessagePreview)
        else -> conversation.lastMessagePreview
    }

    val today = remember { LocalDate.now() }
    val todayLabel = stringResource(R.string.message_date_today)
    val yesterdayLabel = stringResource(R.string.message_date_yesterday)
    val timeLabel = remember(conversation.lastMessageAt, todayLabel, yesterdayLabel) {
        val instant = conversation.lastMessageAt ?: return@remember ""
        val date = instant.atZone(ZoneId.systemDefault())
        val localDate = date.toLocalDate()
        when (localDate) {
            today -> date.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            today.minusDays(1) -> yesterdayLabel
            else -> date.format(DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault()))
        }
    }

    val unreadColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (showUnread)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (showUnread) Dimens.CardBorderWidth else Dimens.CardBorderWidth,
            color = if (showUnread)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarImage(
            avatarUrl = avatarUrl,
            displayName = peerLabel,
            size = Dimens.AvatarMd,
        )
        Spacer(modifier = Modifier.width(Dimens.SpaceMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peerLabel,
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
        if (timeLabel.isNotEmpty()) {
            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showUnread) {
                    Spacer(modifier = Modifier.size(Dimens.SpaceXxs))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(unreadColor, CircleShape),
                    )
                }
            }
        }
    }
    }
}
