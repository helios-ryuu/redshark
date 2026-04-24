package com.helios.redshark.ui.feature.interaction.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConversationNewScreen(
    peerId: String,
    onResolved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationNewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(peerId) {
        viewModel.resolve(peerId)
    }

    LaunchedEffect(uiState.conversationId) {
        uiState.conversationId?.let(onResolved)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            Text(errorMessage, style = MaterialTheme.typography.bodyLarge)
        } else {
            CircularProgressIndicator()
        }
    }
}

