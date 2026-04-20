package com.helios.redshark.ui.myideas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import java.util.UUID

@Composable
fun MyIdeasScreen(
    onIdeaClick: (UUID) -> Unit,
    onCreateIdea: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyIdeasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        // TC-C24: show tag filter chips only when ideas have distinct tags
        val allTags = uiState.allIdeas.flatMap { it.tagIds }.distinct()
        if (allTags.size > 1) {
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
                allTags.forEach { tagId ->
                    FilterChip(
                        selected = uiState.activeTagFilter == tagId,
                        onClick = { viewModel.filterByTag(tagId) },
                        label = { Text(tagId.toString().take(8)) },
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
                uiState.displayedIdeas.isEmpty() -> Text(
                    text = "Chưa có idea nào. Nhấn + để tạo mới.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.displayedIdeas, key = { it.id.toString() }) { idea ->
                        IdeaCard(idea = idea, onClick = { onIdeaClick(idea.id) })
                    }
                }
            }
            FloatingActionButton(
                onClick = onCreateIdea,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo Idea")
            }
        }
    }
}

@Composable
private fun IdeaCard(idea: Idea, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = idea.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IdeaStatusBadge(idea.status)
            }
            idea.description?.let {
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
fun IdeaStatusBadge(status: IdeaStatus) {
    val (label, color) = when (status) {
        IdeaStatus.ACTIVE -> "ACTIVE" to MaterialTheme.colorScheme.primary
        IdeaStatus.CLOSED -> "CLOSED" to MaterialTheme.colorScheme.secondary
        IdeaStatus.CANCELLED -> "CANCELLED" to MaterialTheme.colorScheme.error
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

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}
