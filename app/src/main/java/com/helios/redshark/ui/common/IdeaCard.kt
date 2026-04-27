package com.helios.redshark.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.helios.redshark.R
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.ui.theme.Dimens

@Composable
fun IdeaCard(
    idea: Idea,
    onClick: () -> Unit,
    onUpvote: () -> Unit = {},
    onDownvote: () -> Unit = {},
    onComment: () -> Unit = {},
    onShare: () -> Unit = {},
    imageUrl: String? = null,
    upvoteCount: Int = 0,
    commentCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Avatar + author + title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.SpaceMd, end = Dimens.SpaceMd, top = Dimens.SpaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarImage(
                    avatarUrl = null,
                    displayName = idea.authorId,
                    size = Dimens.AvatarSm,
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Column {
                    Text(
                        text = idea.authorId.take(8),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = idea.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            // Body: description
            idea.description?.let { desc ->
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    modifier = Modifier.padding(horizontal = Dimens.SpaceMd),
                )
            }

            // Optional image below text
            imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    contentScale = ContentScale.Crop,
                )
            }

            // Footer: action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onUpvote,
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceXs),
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = stringResource(R.string.idea_action_upvote),
                        modifier = Modifier.size(Dimens.IconSm),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceXxs))
                    Text(text = upvoteCount.toString(), style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onDownvote) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = stringResource(R.string.idea_action_downvote),
                        modifier = Modifier.size(Dimens.IconSm),
                    )
                }
                TextButton(
                    onClick = onComment,
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceXs),
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.idea_action_comment),
                        modifier = Modifier.size(Dimens.IconSm),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceXxs))
                    Text(text = commentCount.toString(), style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.idea_action_share),
                        modifier = Modifier.size(Dimens.IconSm),
                    )
                }
            }
        }
    }
}
