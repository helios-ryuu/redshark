package com.helios.redshark.ui.message

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Message
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.UUID

private sealed interface ConvListItem {
    data class DateSeparator(val date: LocalDate) : ConvListItem
    data class MessageItem(val message: Message, val showAvatar: Boolean) : ConvListItem
}

private val IdeaLinkRegex = Regex("redshark://idea/[0-9a-fA-F-]{36}")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: UUID,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onOpenIdea: (UUID) -> Unit,
    viewModel: MessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.convState.collectAsStateWithLifecycle()
    val listUiState by viewModel.listState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val defaultUriHandler = LocalUriHandler.current
    val internalUriHandler = remember(defaultUriHandler, onOpenIdea) {
        object : UriHandler {
            override fun openUri(uri: String) {
                if (uri.startsWith("redshark://idea/")) {
                    val ideaId = uri.substringAfter("redshark://idea/")
                    runCatching { UUID.fromString(ideaId) }
                        .onSuccess(onOpenIdea)
                        .onFailure { defaultUriHandler.openUri(uri) }
                } else {
                    defaultUriHandler.openUri(uri)
                }
            }
        }
    }

    val peerUser = remember(listUiState.conversations, conversationId, currentUserId, listUiState.usersById) {
        listUiState.conversations
            .firstOrNull { it.id == conversationId }
            ?.participantIds?.firstOrNull { it != currentUserId }
            ?.let { peerId -> listUiState.usersById[peerId] }
    }

    val peerDisplayName = peerUser?.displayName
        ?: stringResource(R.string.message_conversation_title)

    val listItems by remember(uiState.messages) {
        derivedStateOf {
            buildConvListItems(uiState.messages, currentUserId)
        }
    }

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    LaunchedEffect(listItems.size) {
        if (listItems.isNotEmpty()) {
            listState.animateScrollToItem(listItems.lastIndex)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        peerUser?.let { user ->
                            AvatarImage(
                                avatarUrl = user.avatarUrl,
                                displayName = user.displayName,
                                size = Dimens.AvatarSm,
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                        }
                        Text(peerDisplayName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> LoadingContent()
                    uiState.messages.isEmpty() -> EmptyContent(
                        message = stringResource(R.string.message_thread_empty),
                    )
                    else -> CompositionLocalProvider(LocalUriHandler provides internalUriHandler) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            itemsIndexed(listItems, key = { _, item ->
                                when (item) {
                                    is ConvListItem.DateSeparator -> "sep_${item.date}"
                                    is ConvListItem.MessageItem -> item.message.id.toString()
                                }
                            }) { _, item ->
                                when (item) {
                                    is ConvListItem.DateSeparator -> DateSeparatorRow(item.date)
                                    is ConvListItem.MessageItem -> MessageBubble(
                                        message = item.message,
                                        isCurrentUser = item.message.senderId == currentUserId,
                                        showAvatar = item.showAvatar,
                                        avatarUrl = listUiState.usersById[item.message.senderId]?.avatarUrl,
                                        displayName = listUiState.usersById[item.message.senderId]?.displayName,
                                    )
                                }
                            }
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

            // Input bar — flat Fluent style
            Surface(
                shadowElevation = Dimens.CardElevation,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(Dimens.InputFieldCorner),
                            )
                            .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.message_input_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            maxLines = 4,
                            enabled = !uiState.isSending,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    if (uiState.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(Dimens.IconMd), strokeWidth = 2.dp)
                    } else {
                        FilledIconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(conversationId, inputText.trim(), currentUserId)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.action_send),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildConvListItems(messages: List<Message>, currentUserId: String): List<ConvListItem> {
    val result = mutableListOf<ConvListItem>()
    var lastDate: LocalDate? = null
    messages.forEachIndexed { index, message ->
        val date = message.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
        if (date != lastDate) {
            result += ConvListItem.DateSeparator(date)
            lastDate = date
        }
        val nextMessage = messages.getOrNull(index + 1)
        val isLastInGroup = nextMessage == null || nextMessage.senderId != message.senderId
        val showAvatar = message.senderId != currentUserId && isLastInGroup
        result += ConvListItem.MessageItem(message, showAvatar)
    }
    return result
}

@Composable
private fun DateSeparatorRow(date: LocalDate) {
    val today = remember { LocalDate.now() }
    val todayLabel = stringResource(R.string.message_date_today)
    val yesterdayLabel = stringResource(R.string.message_date_yesterday)
    val label = remember(date, today, todayLabel, yesterdayLabel) {
        when (date) {
            today -> todayLabel
            today.minusDays(1) -> yesterdayLabel
            else -> date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimens.SpaceSm),
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    showAvatar: Boolean = false,
    avatarUrl: String? = null,
    displayName: String? = null,
) {
    val sentShape = RoundedCornerShape(
        topStart = Dimens.MessageBubbleCornerLg,
        topEnd = Dimens.MessageBubbleCornerSm,
        bottomStart = Dimens.MessageBubbleCornerLg,
        bottomEnd = Dimens.MessageBubbleCornerTail,
    )
    val receivedShape = RoundedCornerShape(
        topStart = Dimens.MessageBubbleCornerSm,
        topEnd = Dimens.MessageBubbleCornerLg,
        bottomStart = Dimens.MessageBubbleCornerTail,
        bottomEnd = Dimens.MessageBubbleCornerLg,
    )

    val bubbleColor: Color
    val textColor: Color
    val timestampColor: Color
    val alignment: Alignment.Horizontal

    if (isCurrentUser) {
        bubbleColor = MaterialTheme.colorScheme.primary
        textColor = MaterialTheme.colorScheme.onPrimary
        timestampColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        alignment = Alignment.End
    } else {
        bubbleColor = MaterialTheme.colorScheme.surface
        textColor = MaterialTheme.colorScheme.onSurface
        timestampColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        alignment = Alignment.Start
    }

    val linkColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val annotatedMessage = remember(message.content, linkColor) {
        buildMessageAnnotatedString(message.content, linkColor)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isCurrentUser) {
            if (showAvatar) {
                AvatarImage(
                    avatarUrl = avatarUrl,
                    displayName = displayName ?: message.senderId,
                    size = Dimens.AvatarSm,
                )
            } else {
                Spacer(modifier = Modifier.size(Dimens.AvatarSm))
            }
            Spacer(modifier = Modifier.width(Dimens.SpaceXs))
        }

        Column(horizontalAlignment = alignment) {
            Surface(
                shape = if (isCurrentUser) sentShape else receivedShape,
                color = bubbleColor,
                modifier = Modifier.widthIn(max = Dimens.MessageMaxWidth),
            ) {
                Column(modifier = Modifier.padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm)) {
                    Text(
                        text = annotatedMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                    )
                    Text(
                        text = message.createdAt.atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                        style = MaterialTheme.typography.labelSmall,
                        color = timestampColor,
                    )
                }
            }
        }
    }
}

private fun buildMessageAnnotatedString(message: String, linkColor: Color): AnnotatedString {
    if (!IdeaLinkRegex.containsMatchIn(message)) {
        return AnnotatedString(message)
    }

    return buildAnnotatedString {
        var lastIndex = 0
        for (match in IdeaLinkRegex.findAll(message)) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > lastIndex) {
                append(message.substring(lastIndex, start))
            }
            val url = match.value
            val linkStart = length
            append(url)
            val linkEnd = length
            addLink(LinkAnnotation.Url(url), linkStart, linkEnd)
            addStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
                linkStart,
                linkEnd,
            )
            lastIndex = end
        }
        if (lastIndex < message.length) {
            append(message.substring(lastIndex))
        }
    }
}

