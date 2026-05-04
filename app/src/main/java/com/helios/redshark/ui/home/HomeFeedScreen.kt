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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import com.helios.redshark.R
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.IdeaCard
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@Composable
fun HomeFeedScreen(
    onIdeaClick: (UUID) -> Unit,
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
            uiState.ideas.isEmpty() -> EmptyContent(
                message = stringResource(R.string.home_feed_empty),
                subtitle = stringResource(R.string.home_feed_empty_subtitle),
                icon = Icons.Outlined.Lightbulb,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                items(uiState.ideas, key = { it.id.toString() }) { idea ->
                    IdeaCard(idea = idea, onClick = { onIdeaClick(idea.id) })
                }
            }
        }
    }
}
