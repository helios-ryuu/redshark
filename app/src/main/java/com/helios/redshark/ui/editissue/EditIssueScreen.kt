package com.helios.redshark.ui.editissue

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.domain.model.IssuePriority
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIssueScreen(
    issueId: UUID,
    onNavigateBack: () -> Unit,
    viewModel: EditIssueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(IssuePriority.MEDIUM) }
    var assigneeId by remember { mutableStateOf<String?>(null) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(issueId) { viewModel.loadIssue(issueId) }

    LaunchedEffect(uiState) {
        if (uiState is EditIssueUiState.Loaded && !initialized) {
            val issue = (uiState as EditIssueUiState.Loaded).issue
            title = issue.title
            description = issue.description ?: ""
            priority = issue.priority
            assigneeId = issue.assigneeId
            initialized = true
        }
        if (uiState is EditIssueUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sửa Issue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            EditIssueUiState.Loading, EditIssueUiState.Idle -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is EditIssueUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (uiState as EditIssueUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val validationError = (uiState as? EditIssueUiState.ValidationError)?.message

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề *") },
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null) Text(validationError, color = MaterialTheme.colorScheme.error)
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

                // Priority dropdown
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

                // TC-C16: Assignee picker — shows real user display names
                val assigneeLabel = users.find { it.id == assigneeId }?.displayName
                    ?: if (assigneeId != null) assigneeId!!.take(8) else "Không có"
                ExposedDropdownMenuBox(
                    expanded = assigneeExpanded,
                    onExpandedChange = { assigneeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = assigneeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assignee") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = assigneeExpanded,
                        onDismissRequest = { assigneeExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Không có") },
                            onClick = { assigneeId = null; assigneeExpanded = false },
                        )
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.displayName) },
                                onClick = { assigneeId = user.id; assigneeExpanded = false },
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.saveIssue(
                            id = issueId,
                            title = title.trim(),
                            description = description.trim().ifBlank { null },
                            priority = priority,
                            assigneeId = assigneeId,
                        )
                    },
                    enabled = uiState !is EditIssueUiState.Loading && title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Lưu thay đổi")
                }
            }
        }
    }
}
