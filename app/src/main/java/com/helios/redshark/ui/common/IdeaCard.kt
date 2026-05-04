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
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.helios.redshark.R
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.ui.theme.Dimens
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val today = remember { LocalDate.now() }
    val createdDate = remember(idea.createdAt) {
        idea.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val todayLabel = stringResource(R.string.message_date_today)
    val yesterdayLabel = stringResource(R.string.message_date_yesterday)
    val dateLabel = remember(createdDate, today) {
        when (createdDate) {
            today -> todayLabel
            today.minusDays(1) -> yesterdayLabel
            else -> createdDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevationRaised,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Avatar + author + title | status pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.SpaceLg, end = Dimens.SpaceLg, top = Dimens.SpaceLg),
                verticalAlignment = Alignment.Top,
            ) {
                AvatarImage(
                    avatarUrl = null,
                    displayName = idea.authorId,
                    size = Dimens.AvatarSm,
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = idea.authorId.take(8),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = idea.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                IdeaStatusPill(status = idea.status)
            }

            // Date
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceLg),
            )

            // Body: description
            idea.description?.let { desc ->
                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
                )
            }

            // Collaborator count
            if (idea.collaboratorIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.SpaceLg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconSm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceXxs))
                    Text(
                        text = idea.collaboratorIds.size.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Optional image — edge-to-edge for visual impact
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
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceXs, vertical = Dimens.SpaceXxs),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onUpvote,
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceSm),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbUp,
                        contentDescription = stringResource(R.string.idea_action_upvote),
                        modifier = Modifier.size(Dimens.IconSm),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceXxs))
                    Text(
                        text = upvoteCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDownvote) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbDown,
                        contentDescription = stringResource(R.string.idea_action_downvote),
                        modifier = Modifier.size(Dimens.IconSm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = onComment,
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceSm),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubble,
                        contentDescription = stringResource(R.string.idea_action_comment),
                        modifier = Modifier.size(Dimens.IconSm),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceXxs))
                    Text(text = commentCount.toString(), style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.idea_action_share),
                        modifier = Modifier.size(Dimens.IconSm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
