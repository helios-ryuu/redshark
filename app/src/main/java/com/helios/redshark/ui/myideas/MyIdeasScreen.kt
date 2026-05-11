package com.helios.redshark.ui.myideas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaReaction
import com.helios.redshark.R
import com.helios.redshark.ui.common.EmptyContent
import com.helios.redshark.ui.common.ErrorContent
import com.helios.redshark.ui.common.IdeaCard
import com.helios.redshark.ui.common.LoadingContent
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@Composable
fun MyIdeasScreen(
    onIdeaClick: (UUID) -> Unit,
    onCreateIdea: () -> Unit,
    onCommentClick: (UUID) -> Unit,
    onShareClick: (Idea) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyIdeasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        val allTags = uiState.allIdeas.flatMap { it.tagIds }.distinct()
        if (allTags.size > 1) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = Dimens.SpaceXxs),
            ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs + Dimens.SpaceXxs),
            ) {
                FilterChip(
                    selected = uiState.activeTagFilter == null,
                    onClick = { viewModel.filterByTag(null) },
                    label = { Text(stringResource(R.string.ideas_filter_all)) },
                )
                allTags.forEach { tagId ->
                    FilterChip(
                        selected = uiState.activeTagFilter == tagId,
                        onClick = { viewModel.filterByTag(tagId) },
                        label = { Text(tagId.toString().take(8)) },
                    )
                }
            } // end Row
            } // end Surface
            HorizontalDivider()
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.errorMessage != null -> ErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = viewModel::retry,
                )
                uiState.displayedIdeas.isEmpty() -> EmptyContent(
                    message = stringResource(R.string.ideas_empty),
                    icon = Icons.Outlined.Lightbulb,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = Dimens.SpaceLg,
                        end = Dimens.SpaceLg,
                        top = Dimens.SpaceLg,
                        bottom = Dimens.ListBottomPaddingWithFab,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    items(uiState.displayedIdeas, key = { it.id.toString() }) { idea ->
                        val reaction = uiState.reactionStates[idea.id] ?: IdeaReaction.NONE
                        val upvoteDelta = uiState.upvoteDeltas[idea.id] ?: 0
                        val upvoteCount = (idea.upvoteCount + upvoteDelta).coerceAtLeast(0)
                        val commentCount = uiState.commentCounts[idea.id] ?: idea.commentCount
                        IdeaCard(
                            idea = idea,
                            onClick = { onIdeaClick(idea.id) },
                            onUpvote = { viewModel.toggleUpvote(idea.id) },
                            onDownvote = { viewModel.toggleDownvote(idea.id) },
                            onComment = { onCommentClick(idea.id) },
                            onShare = { onShareClick(idea) },
                            upvoteCount = upvoteCount,
                            commentCount = commentCount,
                            isUpvoted = reaction == IdeaReaction.UPVOTED,
                            isDownvoted = reaction == IdeaReaction.DOWNVOTED,
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = onCreateIdea,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.SpaceLg),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ideas_fab_create_cd))
            }
        }
    }
}
