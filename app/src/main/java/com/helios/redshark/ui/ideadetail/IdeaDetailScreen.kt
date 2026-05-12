package com.helios.redshark.ui.ideadetail

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.IdeaStatusPill
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.IssueCard
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.navigation.Routes
import com.helios.redshark.ui.theme.Dimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaDetailScreen(
    ideaId: UUID,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onEditIdea: (UUID) -> Unit,
    onCreateIssue: (UUID) -> Unit,
    onIssueClick: (UUID) -> Unit,
    viewModel: IdeaDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val shareLink = remember(ideaId) { Routes.ideaDeepLink(ideaId.toString()) }
    val ideaTitle = uiState.idea?.title ?: stringResource(R.string.idea_detail_title_fallback)
    val shareText = stringResource(
        R.string.message_share_idea_template_title_only,
        ideaTitle,
        shareLink,
    )
    val shareChooserTitle = stringResource(R.string.idea_share_link_title)

    LaunchedEffect(ideaId) {
        viewModel.loadIdea(ideaId, currentUserId)
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) onNavigateBack()
    }

    LaunchedEffect(uiState.collabRequestState) {
        if (uiState.collabRequestState is CollabRequestState.Sent ||
            uiState.collabRequestState is CollabRequestState.Error
        ) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearCollabRequestState()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.idea_delete_dialog_title)) },
            text = { Text(stringResource(R.string.idea_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteIdea(ideaId)
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.idea?.title ?: stringResource(R.string.idea_detail_title_fallback),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                shareChooserTitle,
                            ),
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.idea_share_link_cd))
                    }
                    if (uiState.isCurrentUserAuthor) {
                        IconButton(onClick = { onEditIdea(ideaId) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoadingIdea ->
                LoadingContent(modifier = Modifier.fillMaxSize().padding(padding))

            uiState.errorMessage != null && uiState.idea == null ->
                ErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadIdea(ideaId, currentUserId) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )

            else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Dimens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    uiState.idea?.let { idea ->
                        // Author + status hero
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Box(modifier = Modifier.padding(Dimens.SpaceMd)) {
                                    IdeaAuthorRow(idea = idea)
                                }
                            }
                        }

                        // Description
                        idea.description?.let { desc ->
                            item {
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        // Author status management (close / cancel)
                        if (uiState.isCurrentUserAuthor && idea.status == IdeaStatus.ACTIVE) {
                            item {
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                                        OutlinedButton(onClick = { viewModel.changeStatus(ideaId, IdeaStatus.CLOSED) }) {
                                            Text(stringResource(R.string.idea_action_close))
                                        }
                                        OutlinedButton(onClick = { viewModel.changeStatus(ideaId, IdeaStatus.CANCELLED) }) {
                                            Text(stringResource(R.string.idea_action_cancel))
                                        }
                                    }
                                    uiState.statusUpdateError?.let { InlineErrorText(it) }
                                }
                            }
                        }

                        // Collab request (non-author, active idea, not already collaborator)
                        if (!uiState.isCurrentUserAuthor &&
                            idea.status == IdeaStatus.ACTIVE &&
                            currentUserId !in idea.collaboratorIds
                        ) {
                            item {
                                val collabState = uiState.collabRequestState
                                Column {
                                    Button(
                                        onClick = { viewModel.requestCollab(ideaId) },
                                        enabled = collabState !is CollabRequestState.Sending && collabState !is CollabRequestState.Sent,
                                    ) {
                                        when (collabState) {
                                            is CollabRequestState.Sending ->
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            is CollabRequestState.Sent ->
                                                Text(stringResource(R.string.idea_action_collab_sent))
                                            else ->
                                                Text(stringResource(R.string.idea_action_request_collab))
                                        }
                                    }
                                    if (collabState is CollabRequestState.Error) {
                                        InlineErrorText(collabState.message)
                                    }
                                }
                            }
                        }
                    }

                    // Issues section
                    item {
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        IdeaSectionHeader(
                            title = stringResource(R.string.idea_section_issues, uiState.issues.size),
                            action = {
                                IconButton(onClick = { onCreateIssue(ideaId) }) {
                                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.idea_section_issues_add_cd))
                                }
                            },
                        )
                    }

                    if (uiState.issues.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.idea_issues_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = Dimens.SpaceXs),
                            )
                        }
                    } else {
                        items(uiState.issues, key = { it.id.toString() }) { issue ->
                            IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
                        }
                    }

                    // Comments section
                    item {
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        IdeaSectionHeader(
                            title = stringResource(R.string.idea_section_comments, uiState.comments.size),
                        )
                    }

                    items(uiState.comments, key = { it.id.toString() }) { comment ->
                        CommentItem(comment)
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.SpaceXs)) }
                }

                uiState.errorMessage?.let { message ->
                    InlineErrorText(
                        message = message,
                        modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
                    )
                }

                CommentInput(
                    value = commentText,
                    onValueChange = { commentText = it },
                    isSubmitting = uiState.commentSubmitState is CommentSubmitState.Submitting,
                    onSend = {
                        if (commentText.isNotBlank()) {
                            viewModel.sendComment(ideaId, commentText.trim(), currentUserId)
                            commentText = ""
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun IdeaAuthorRow(idea: Idea) {
    val dateLabel = remember(idea.createdAt) {
        idea.createdAt
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarImage(
            avatarUrl = null,
            displayName = idea.authorId,
            size = Dimens.AvatarSm,
        )
        Spacer(modifier = Modifier.width(Dimens.SpaceSm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = idea.authorId.take(8),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IdeaStatusPill(status = idea.status)
    }
}

@Composable
private fun IdeaSectionHeader(
    title: String,
    action: (@Composable () -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            action?.invoke()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    val timeLabel = remember(comment.createdAt) {
        comment.createdAt
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
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
                CircularProgressIndicator(modifier = Modifier.size(Dimens.IconMd), strokeWidth = 2.dp)
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
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.action_send))
                }
            }
        }
    }
}
