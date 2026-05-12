package com.helios.redshark.ui.issuedetail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.ISSUE_STATE_MACHINE
import com.helios.redshark.domain.model.User
import com.helios.redshark.ui.common.AvatarImage
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
                uiState.errorMessage?.let { message ->
                ErrorContent(
                    message = message,
                    onRetry = { viewModel.loadIssue(issueId, currentUserId) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }

            else -> uiState.issue?.let { issue ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(Dimens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
                ) {
                    // Status + Priority card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.SpaceMd),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IssueStatusPill(issue.status)
                            IssuePriorityPill(issue.priority)
                        }
                    }

                    // Description card
                    issue.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(Dimens.SpaceLg),
                            )
                        }
                    }

                    // Assignee section
                    if (uiState.assigneeUser != null || issue.assigneeId != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                            Text(
                                text = stringResource(R.string.issue_field_assignee),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AssigneeRow(user = uiState.assigneeUser, fallbackId = issue.assigneeId)
                        }
                    }

                    // View idea link
                    AssistChip(
                        onClick = { onViewIdea(issue.ideaId) },
                        label = { Text(stringResource(R.string.issue_action_view_idea)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                            )
                        },
                    )

                    // Status change section (owner only)
                    if (uiState.canEdit) {
                        val availableStatuses = ISSUE_STATE_MACHINE[issue.status] ?: emptySet()
                        if (availableStatuses.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(Dimens.SpaceMd),
                                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                                ) {
                                    Text(
                                        text = stringResource(R.string.issue_section_status_change),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        availableStatuses.forEach { newStatus ->
                                            FilterChip(
                                                selected = false,
                                                onClick = { viewModel.updateStatus(issueId, newStatus) },
                                                enabled = uiState.statusUpdateState !is StatusUpdateState.Updating,
                                                label = {
                                                    Text(
                                                        text = newStatus.name
                                                            .lowercase()
                                                            .replace('_', ' ')
                                                            .split(' ')
                                                            .joinToString(" ") { it.capitalize(Locale.current) },
                                                        style = MaterialTheme.typography.labelMedium,
                                                    )
                                                },
                                            )
                                        }
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

                    Spacer(modifier = Modifier.height(Dimens.SpaceLg))
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
        AvatarImage(
            avatarUrl = user?.avatarUrl,
            displayName = user?.displayName ?: fallbackId?.take(1) ?: "?",
            size = Dimens.AvatarSm,
        )
        Text(
            text = user?.displayName ?: fallbackId?.take(8) ?: "",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
