package com.helios.redshark.ui.ideadetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.IdeaStatusPill
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.IssueCard
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
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
                title = { Text(uiState.idea?.title ?: stringResource(R.string.idea_detail_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val link = "redshark://idea/$ideaId"
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("idea_link", link))
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
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                            ) {
                                IdeaStatusPill(idea.status)
                            }
                        }
                        if (uiState.isCurrentUserAuthor && idea.status == IdeaStatus.ACTIVE) {
                            item {
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
                        idea.description?.let { desc ->
                            item {
                                Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.idea_section_issues, uiState.issues.size),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            IconButton(onClick = { onCreateIssue(ideaId) }) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.idea_section_issues_add_cd))
                            }
                        }
                        HorizontalDivider()
                    }

                    if (uiState.issues.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.idea_issues_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(uiState.issues, key = { it.id.toString() }) { issue ->
                            IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        Text(
                            text = stringResource(R.string.idea_section_comments, uiState.comments.size),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        HorizontalDivider()
                    }

                    items(uiState.comments, key = { it.id.toString() }) { comment ->
                        CommentItem(comment)
                    }
                }

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
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
private fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = comment.authorId.take(8),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.padding(top = Dimens.SpaceSm))
    }
}

@Composable
private fun CommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSend: () -> Unit,
) {
    Surface(shadowElevation = Dimens.CardElevationRaised) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(stringResource(R.string.idea_comment_placeholder)) },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                enabled = !isSubmitting,
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.action_send))
                }
            }
        }
    }
}
