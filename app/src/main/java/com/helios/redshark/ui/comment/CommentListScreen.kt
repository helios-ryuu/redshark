package com.helios.redshark.ui.comment

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentListScreen(
    ideaId: UUID,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(ideaId) {
        viewModel.load(ideaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.comment_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        CommentBody(
            uiState = uiState,
            commentText = commentText,
            onCommentTextChange = { commentText = it },
            onRetry = { viewModel.retry(ideaId) },
            onSend = {
                if (commentText.isNotBlank()) {
                    viewModel.sendComment(ideaId, commentText.trim(), currentUserId)
                    commentText = ""
                }
            },
            modifier = modifier.fillMaxSize().padding(padding),
        )
    }
}

@Composable
fun CommentSheetContent(
    ideaId: UUID,
    currentUserId: String,
    modifier: Modifier = Modifier,
    viewModel: CommentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(ideaId) {
        viewModel.load(ideaId)
    }

    CommentBody(
        uiState = uiState,
        commentText = commentText,
        onCommentTextChange = { commentText = it },
        onRetry = { viewModel.retry(ideaId) },
        onSend = {
            if (commentText.isNotBlank()) {
                viewModel.sendComment(ideaId, commentText.trim(), currentUserId)
                commentText = ""
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CommentBody(
    uiState: CommentUiState,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onRetry: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.errorMessage != null -> ErrorContent(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                )
                uiState.comments.isEmpty() -> EmptyContent(
                    message = stringResource(R.string.comment_empty),
                    subtitle = stringResource(R.string.comment_empty_subtitle),
                    icon = Icons.Outlined.ChatBubble,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(Dimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    items(uiState.comments, key = { it.id.toString() }) { comment ->
                        CommentItem(comment)
                    }
                }
            }
        }
        uiState.submitError?.let {
            InlineErrorText(
                message = it,
                modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
            )
        }
        CommentInput(
            value = commentText,
            onValueChange = onCommentTextChange,
            isSubmitting = uiState.isSubmitting,
            onSend = onSend,
        )
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    val timeLabel = remember(comment.createdAt) {
        comment.createdAt
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = Dimens.CardBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceMd),
            verticalAlignment = Alignment.Top,
        ) {
            AvatarImage(
                avatarUrl = null,
                displayName = comment.authorId,
                size = Dimens.AvatarSm,
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = comment.authorId.take(8),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSend: () -> Unit,
) {
    Surface(shadowElevation = 4.dp, tonalElevation = 2.dp) {
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
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.idea_comment_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    maxLines = 3,
                    enabled = !isSubmitting,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.IconMd),
                    strokeWidth = 2.dp,
                )
            } else {
                FilledIconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.action_send),
                    )
                }
            }
        }
    }
}
