package com.helios.redshark.ui.createissue

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.domain.model.IssuePriority
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    ideaId: UUID,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onCreated: (UUID) -> Unit,
    viewModel: CreateIssueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(IssuePriority.MEDIUM) }
    var priorityExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is CreateIssueUiState.Success) {
            onCreated((uiState as CreateIssueUiState.Success).issueId)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo Issue mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val titleError = (uiState as? CreateIssueUiState.Failure.ValidationError)?.message

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề *") },
                isError = titleError != null,
                supportingText = {
                    if (titleError != null) Text(titleError, color = MaterialTheme.colorScheme.error)
                    else Text("${title.length}/120")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4,
            )

            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = it },
            ) {
                OutlinedTextField(
                    value = priority.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Độ ưu tiên") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false },
                ) {
                    IssuePriority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = { priority = p; priorityExpanded = false },
                        )
                    }
                }
            }

            when (val s = uiState) {
                is CreateIssueUiState.Failure.LimitExceeded ->
                    Text("Đạt giới hạn 20 issue active.", color = MaterialTheme.colorScheme.error)
                is CreateIssueUiState.Failure.IdeaNotActive ->
                    Text("Idea này đã đóng hoặc hủy, không thể thêm issue.", color = MaterialTheme.colorScheme.error)
                is CreateIssueUiState.Failure.GenericError ->
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                else -> Unit
            }

            Button(
                onClick = {
                    viewModel.createIssue(
                        ideaId = ideaId,
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                        priority = priority,
                        currentUserId = currentUserId,
                    )
                },
                enabled = uiState !is CreateIssueUiState.Loading && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState is CreateIssueUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Tạo Issue")
                }
            }
        }
    }
}
