package com.helios.redshark.ui.createissue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.theme.Dimens
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
                title = { Text(stringResource(R.string.issue_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
        ) {
            val titleError = (uiState as? CreateIssueUiState.Failure.ValidationError)?.message

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.issue_field_title)) },
                isError = titleError != null,
                supportingText = {
                    if (titleError != null) Text(titleError, color = MaterialTheme.colorScheme.error)
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

            when (val s = uiState) {
                is CreateIssueUiState.Failure.LimitExceeded ->
                    InlineErrorText(stringResource(R.string.issue_error_limit_exceeded))
                is CreateIssueUiState.Failure.IdeaNotActive ->
                    InlineErrorText(stringResource(R.string.issue_error_idea_not_active))
                is CreateIssueUiState.Failure.GenericError ->
                    InlineErrorText(s.message)
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
                    Text(stringResource(R.string.issue_action_create))
                }
            }
        }
    }
}
