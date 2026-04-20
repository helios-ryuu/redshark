package com.helios.redshark.ui.issuedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.helios.redshark.domain.model.ISSUE_STATE_MACHINE
import com.helios.redshark.domain.model.IssueStatus
import com.helios.redshark.domain.model.User
import com.helios.redshark.ui.home.PriorityChip
import com.helios.redshark.ui.myideas.ErrorContent
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: UUID,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onEditIssue: (UUID) -> Unit,
    onViewIdea: (UUID) -> Unit = {},
    viewModel: IssueDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(issueId) { viewModel.loadIssue(issueId, currentUserId) }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) onNavigateBack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa Issue") },
            text = { Text("Bạn có chắc muốn xóa issue này không?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteIssue(issueId)
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
                title = { Text(uiState.issue?.title ?: "Chi tiết Issue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (uiState.canEdit) {
                        IconButton(onClick = { onEditIssue(issueId) }) {
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
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.errorMessage != null && uiState.issue == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                ErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadIssue(issueId, currentUserId) },
                )
            }

            else -> uiState.issue?.let { issue ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IssueStatusChip(issue.status)
                        PriorityChip(issue.priority)
                    }

                    issue.description?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }

                    // TC-C16: show assignee avatar + name
                    if (uiState.assigneeUser != null || issue.assigneeId != null) {
                        AssigneeRow(user = uiState.assigneeUser, fallbackId = issue.assigneeId)
                    }

                    // TC-C06: Navigate to parent idea — allows testing non-owner idea access
                    TextButton(onClick = { onViewIdea(issue.ideaId) }) {
                        Text("Xem Idea gốc")
                    }

                    HorizontalDivider()

                    if (uiState.canEdit) {
                        val availableStatuses = ISSUE_STATE_MACHINE[issue.status] ?: emptySet()
                        if (availableStatuses.isNotEmpty()) {
                            Text("Chuyển trạng thái:", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                availableStatuses.forEach { newStatus ->
                                    OutlinedButton(
                                        onClick = { viewModel.updateStatus(issueId, newStatus) },
                                        enabled = uiState.statusUpdateState !is StatusUpdateState.Updating,
                                    ) {
                                        Text(newStatus.name)
                                    }
                                }
                            }
                        }
                    }

                    when (val s = uiState.statusUpdateState) {
                        is StatusUpdateState.InvalidTransition ->
                            Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        is StatusUpdateState.Error ->
                            Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun AssigneeRow(user: User?, fallbackId: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (user?.avatarUrl != null) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            val initials = user?.displayName?.take(1)?.uppercase() ?: fallbackId?.take(1)?.uppercase() ?: "?"
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Text(
            text = user?.displayName ?: fallbackId?.take(8) ?: "",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun IssueStatusChip(status: IssueStatus) {
    val (label, color) = when (status) {
        IssueStatus.OPEN -> "OPEN" to MaterialTheme.colorScheme.primary
        IssueStatus.IN_PROGRESS -> "IN PROGRESS" to MaterialTheme.colorScheme.tertiary
        IssueStatus.CLOSED -> "CLOSED" to MaterialTheme.colorScheme.secondary
        IssueStatus.CANCELLED -> "CANCELLED" to MaterialTheme.colorScheme.error
    }
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
