package com.helios.redshark.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun HomeFeedScreen(
    onIdeaClick: (UUID) -> Unit,
    onCommentClick: (UUID) -> Unit,
    onShareClick: (Idea) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingContent()
            uiState.errorMessage != null -> uiState.errorMessage?.let { message ->
                ErrorContent(message = message, onRetry = viewModel::retry)
            }
            uiState.ideas.isEmpty() -> EmptyContent(
                message = stringResource(R.string.home_feed_empty),
                subtitle = stringResource(R.string.home_feed_empty_subtitle),
                icon = Icons.Outlined.Lightbulb,
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        Color.Transparent,
                                    )
                                )
                            )
                            .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceXl),
                    ) {
                        Text(
                            text = stringResource(R.string.home_feed_section_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.home_feed_section_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(uiState.ideas, key = { it.id.toString() }) { idea ->
                    val author = uiState.usersById[idea.authorId]
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
                        authorDisplayName = author?.displayName,
                        authorAvatarUrl = author?.avatarUrl,
                        upvoteCount = upvoteCount,
                        commentCount = commentCount,
                        isUpvoted = reaction == IdeaReaction.UPVOTED,
                        isDownvoted = reaction == IdeaReaction.DOWNVOTED,
                        modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
                    )
                }
            }
        }
    }
}
