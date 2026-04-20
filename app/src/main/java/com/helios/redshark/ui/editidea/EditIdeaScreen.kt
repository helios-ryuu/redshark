package com.helios.redshark.ui.editidea

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
                title = { Text("Sửa Idea") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is EditIdeaUiState.Loading, EditIdeaUiState.Idle -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) { CircularProgressIndicator() }

            is EditIdeaUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(
                    text = (uiState as EditIdeaUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val validationError = (uiState as? EditIdeaUiState.ValidationError)?.message

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
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    maxLines = 6,
                )

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
                    Text("Lưu thay đổi")
                }
            }
        }
    }
}
