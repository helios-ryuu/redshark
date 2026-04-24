package com.helios.redshark.ui.ideadetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.ui.home.IssueCard
import com.helios.redshark.ui.myideas.ErrorContent
import com.helios.redshark.ui.myideas.IdeaStatusBadge
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa Idea") },
            text = { Text("Bạn có chắc muốn xóa idea này không?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteIdea(ideaId)
                }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.idea?.title ?: "Chi tiết Idea") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // TC-C23: copy deep link so tester can re-open after deletion
                    IconButton(onClick = {
                        val link = "redshark://idea/$ideaId"
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("idea_link", link))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Copy link")
                    }
                    if (uiState.isCurrentUserAuthor) {
                        IconButton(onClick = { onEditIdea(ideaId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoadingIdea -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.errorMessage != null && uiState.idea == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                ErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadIdea(ideaId, currentUserId) }
                )
            }

            else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.idea?.let { idea ->
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                IdeaStatusBadge(idea.status)
                            }
                        }
                        if (uiState.isCurrentUserAuthor && idea.status == IdeaStatus.ACTIVE) {
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { viewModel.changeStatus(ideaId, IdeaStatus.CLOSED) }) {
                                        Text("Đóng Idea")
                                    }
                                    OutlinedButton(onClick = { viewModel.changeStatus(ideaId, IdeaStatus.CANCELLED) }) {
                                        Text("Hủy Idea")
                                    }
                                }
                                uiState.statusUpdateError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                            Text("Issues (${uiState.issues.size})", style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = { onCreateIssue(ideaId) }) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm Issue")
                            }
                        }
                        HorizontalDivider()
                    }

                    if (uiState.issues.isEmpty()) {
                        item {
                            Text(
                                "Chưa có issue nào.",
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Bình luận (${uiState.comments.size})", style = MaterialTheme.typography.titleSmall)
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
                    }
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
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun CommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSend: () -> Unit,
) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Viết bình luận...") },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                enabled = !isSubmitting,
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
                }
            }
        }
    }
}
