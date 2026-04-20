package com.helios.redshark.ui.createidea

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIdeaScreen(
    onNavigateBack: () -> Unit,
    onCreated: (UUID) -> Unit,
    viewModel: CreateIdeaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf(viewModel.draftTitle) }
    var description by remember { mutableStateOf(viewModel.draftDescription ?: "") }

    LaunchedEffect(uiState) {
        if (uiState is CreateIdeaUiState.Success) {
            onCreated((uiState as CreateIdeaUiState.Success).ideaId)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo Idea mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val titleError = (uiState as? CreateIdeaUiState.Failure.ValidationError)
                ?.message?.takeIf { it.contains("title", ignoreCase = true) }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề *") },
                supportingText = {
                    if (titleError != null) Text(titleError, color = MaterialTheme.colorScheme.error)
                    else Text("${title.length}/120")
                },
                isError = titleError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                maxLines = 6,
            )

            if (uiState is CreateIdeaUiState.Failure.NetworkError) {
                Text(
                    text = (uiState as CreateIdeaUiState.Failure.NetworkError).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (uiState is CreateIdeaUiState.Failure.GenericError) {
                Text(
                    text = (uiState as CreateIdeaUiState.Failure.GenericError).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = {
                    viewModel.createIdea(
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                    )
                },
                enabled = uiState !is CreateIdeaUiState.Loading && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState is CreateIdeaUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Tạo Idea")
                }
            }
        }
    }
}
