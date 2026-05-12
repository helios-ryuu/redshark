package com.helios.redshark.ui.createidea

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.common.InlineErrorText
import com.helios.redshark.ui.theme.Dimens
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
                title = { Text(stringResource(R.string.idea_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.FormBrandStripHeight)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
            val titleError = (uiState as? CreateIdeaUiState.Failure.ValidationError)
                ?.message?.takeIf { it.contains("title", ignoreCase = true) }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.idea_field_title)) },
                supportingText = {
                    if (titleError != null) Text(titleError, color = MaterialTheme.colorScheme.error)
                    else Text("${title.length}/120")
                },
                isError = titleError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
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

            (uiState as? CreateIdeaUiState.Failure.NetworkError)?.let { InlineErrorText(it.message) }
            (uiState as? CreateIdeaUiState.Failure.GenericError)?.let { InlineErrorText(it.message) }

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
                    CircularProgressIndicator(modifier = Modifier.size(Dimens.ButtonProgressIndicatorSize), strokeWidth = Dimens.ButtonProgressIndicatorStroke)
                } else {
                    Text(stringResource(R.string.idea_action_create))
                }
            } // end inner Column
        } // end outer Column
    } // end Scaffold content
} // end Scaffold lambda
}
