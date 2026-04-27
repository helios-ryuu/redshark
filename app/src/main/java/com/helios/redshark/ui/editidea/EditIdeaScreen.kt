package com.helios.redshark.ui.editidea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIdeaScreen(
    ideaId: UUID,
    onNavigateBack: () -> Unit,
    viewModel: EditIdeaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(ideaId) { viewModel.loadIdea(ideaId) }

    LaunchedEffect(uiState) {
        if (uiState is EditIdeaUiState.Loaded && !initialized) {
            val idea = (uiState as EditIdeaUiState.Loaded).idea
            title = idea.title
            description = idea.description ?: ""
            initialized = true
        }
        if (uiState is EditIdeaUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.idea_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            is EditIdeaUiState.Loading, EditIdeaUiState.Idle ->
                LoadingContent(modifier = Modifier.fillMaxSize().padding(padding))

            is EditIdeaUiState.Error ->
                ErrorContent(
                    message = (uiState as EditIdeaUiState.Error).message,
                    onRetry = { viewModel.loadIdea(ideaId) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                val validationError = (uiState as? EditIdeaUiState.ValidationError)?.message

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.idea_field_title)) },
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
                    label = { Text(stringResource(R.string.idea_field_description)) },
                    modifier = Modifier.fillMaxWidth().height(Dimens.InputFieldHeightMultiline),
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )

                (uiState as? EditIdeaUiState.NetworkError)?.let { InlineErrorText(it.message) }

                Button(
                    onClick = {
                        viewModel.saveIdea(
                            id = ideaId,
                            title = title.trim(),
                            description = description.trim().ifBlank { null },
                        )
                    },
                    enabled = uiState !is EditIdeaUiState.Loading && title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_save_changes))
                }
            }
        }
    }
}
