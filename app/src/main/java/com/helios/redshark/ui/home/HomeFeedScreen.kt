package com.helios.redshark.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.ui.myideas.ErrorContent
import java.util.UUID

@Composable
fun HomeFeedScreen(
    onIssueClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        // TC-C24: tag filter — distinct ideaIds from all issues shown as chips
        val ideaIds = uiState.allIssues.map { it.ideaId }.distinct()
        if (ideaIds.size > 1) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = uiState.activeTagFilter == null,
                    onClick = { viewModel.filterByTag(null) },
                    label = { Text("Tất cả") },
                )
                ideaIds.forEach { ideaId ->
                    FilterChip(
                        selected = uiState.activeTagFilter == ideaId,
                        onClick = { viewModel.filterByTag(ideaId) },
                        label = { Text(ideaId.toString().take(8)) },
                    )
                }
            }
            HorizontalDivider()
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.errorMessage != null -> ErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = viewModel::retry,
                    modifier = Modifier.align(Alignment.Center),
                )
                uiState.displayedIssues.isEmpty() -> Text(
                    text = "Không có issue nào từ người dùng khác.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.displayedIssues, key = { it.id.toString() }) { issue ->
                        IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun IssueCard(issue: Issue, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = issue.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                PriorityChip(issue.priority)
            }
            issue.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
fun PriorityChip(priority: IssuePriority) {
    val (label, color) = when (priority) {
        IssuePriority.HIGH -> "CAO" to MaterialTheme.colorScheme.error
        IssuePriority.MEDIUM -> "TB" to MaterialTheme.colorScheme.primary
        IssuePriority.LOW -> "THẤP" to MaterialTheme.colorScheme.secondary
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
