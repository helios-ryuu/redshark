package com.helios.redshark.ui.editissue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
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
                title = { Text(stringResource(R.string.issue_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            EditIssueUiState.Loading, EditIssueUiState.Idle ->
                LoadingContent(modifier = Modifier.fillMaxSize().padding(padding))

            is EditIssueUiState.Error ->
                ErrorContent(
                    message = (uiState as EditIssueUiState.Error).message,
                    onRetry = { viewModel.loadIssue(issueId) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                val validationError = (uiState as? EditIssueUiState.ValidationError)?.message

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.issue_field_title)) },
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null) Text(validationError, color = MaterialTheme.colorScheme.error)
                        else Text("${title.length}/120")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                singleLine = true,
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.issue_field_description)) },
                    modifier = Modifier.fillMaxWidth().height(Dimens.InputFieldHeightMultilineSm),
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
                        label = { Text(stringResource(R.string.issue_field_priority)) },
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

                val assigneeNoneLabel = stringResource(R.string.issue_assignee_none)
                val assigneeLabel = users.find { it.id == assigneeId }?.displayName
                    ?: if (assigneeId != null) assigneeId!!.take(8) else assigneeNoneLabel
                ExposedDropdownMenuBox(
                    expanded = assigneeExpanded,
                    onExpandedChange = { assigneeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = assigneeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.issue_field_assignee)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = assigneeExpanded,
                        onDismissRequest = { assigneeExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(assigneeNoneLabel) },
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

                (uiState as? EditIssueUiState.NetworkError)?.let { InlineErrorText(it.message) }

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
                    Text(stringResource(R.string.action_save_changes))
                }
            }
        }
    }
}
