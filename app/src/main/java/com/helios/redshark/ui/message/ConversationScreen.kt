package com.helios.redshark.ui.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Message
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: UUID,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    viewModel: MessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.convState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.message_conversation_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> LoadingContent()
                    uiState.messages.isEmpty() -> EmptyContent(
                        message = stringResource(R.string.message_thread_empty),
                    )
                    else -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(Dimens.SpaceMd),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(uiState.messages, key = { it.id.toString() }) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == currentUserId,
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let {
                InlineErrorText(
                    message = it,
                    modifier = Modifier.padding(horizontal = Dimens.SpaceMd),
                )
            }

            Surface(shadowElevation = Dimens.CardElevationRaised) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(stringResource(R.string.message_input_placeholder)) },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        enabled = !uiState.isSending,
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(conversationId, inputText.trim(), currentUserId)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isSending,
                    ) {
                        if (uiState.isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.action_send))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm)) {
                Text(text = message.content, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = message.createdAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
