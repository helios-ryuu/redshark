package com.helios.redshark.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.IssueCard
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@Composable
fun HomeFeedScreen(
    onIssueClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> ErrorContent(
                message = uiState.errorMessage!!,
                onRetry = viewModel::retry,
            )
            uiState.issues.isEmpty() -> EmptyContent(message = stringResource(R.string.feed_empty))
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                items(uiState.issues, key = { it.id.toString() }) { issue ->
                    IssueCard(issue = issue, onClick = { onIssueClick(issue.id) })
                }
            }
        }
    }
}
