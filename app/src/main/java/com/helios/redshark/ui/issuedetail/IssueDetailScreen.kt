package com.helios.redshark.ui.issuedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.helios.redshark.R
import com.helios.redshark.domain.model.ISSUE_STATE_MACHINE
import com.helios.redshark.domain.model.User
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.IssuePriorityPill
import com.helios.redshark.ui.common.IssueStatusPill
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
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
            title = { Text(stringResource(R.string.issue_delete_dialog_title)) },
            text = { Text(stringResource(R.string.issue_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteIssue(issueId)
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
                title = { Text(uiState.issue?.title ?: stringResource(R.string.issue_detail_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (uiState.canEdit) {
                        IconButton(onClick = { onEditIssue(issueId) }) {
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
            uiState.isLoading ->
                LoadingContent(modifier = Modifier.fillMaxSize().padding(padding))

            uiState.errorMessage != null && uiState.issue == null ->
                ErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadIssue(issueId, currentUserId) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )

            else -> uiState.issue?.let { issue ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                        IssueStatusPill(issue.status)
                        IssuePriorityPill(issue.priority)
                    }

                    issue.description?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (uiState.assigneeUser != null || issue.assigneeId != null) {
                        AssigneeRow(user = uiState.assigneeUser, fallbackId = issue.assigneeId)
                    }

                    TextButton(onClick = { onViewIdea(issue.ideaId) }) {
                        Text(stringResource(R.string.issue_action_view_idea))
                    }

                    HorizontalDivider()

                    if (uiState.canEdit) {
                        val availableStatuses = ISSUE_STATE_MACHINE[issue.status] ?: emptySet()
                        if (availableStatuses.isNotEmpty()) {
                            Text(stringResource(R.string.issue_section_status_change), style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
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
                        is StatusUpdateState.InvalidTransition -> InlineErrorText(s.message)
                        is StatusUpdateState.Error -> InlineErrorText(s.message)
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun AssigneeRow(user: User?, fallbackId: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        if (user?.avatarUrl != null) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier.size(Dimens.AvatarSm).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            val initials = user?.displayName?.take(1)?.uppercase() ?: fallbackId?.take(1)?.uppercase() ?: "?"
            Box(
                modifier = Modifier.size(Dimens.AvatarSm).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Text(
            text = user?.displayName ?: fallbackId?.take(8) ?: "",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
