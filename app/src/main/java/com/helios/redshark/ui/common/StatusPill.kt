package com.helios.redshark.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.domain.model.IssueStatus
import com.helios.redshark.ui.theme.Dimens

@Composable
fun StatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f),
        modifier = modifier,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXxs),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun IdeaStatusPill(status: IdeaStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        IdeaStatus.ACTIVE -> "ACTIVE" to MaterialTheme.colorScheme.primary
        IdeaStatus.CLOSED -> "CLOSED" to MaterialTheme.colorScheme.secondary
        IdeaStatus.CANCELLED -> "CANCELLED" to MaterialTheme.colorScheme.error
    }
    StatusPill(label = label, color = color, modifier = modifier)
}

@Composable
fun IssueStatusPill(status: IssueStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        IssueStatus.OPEN -> "OPEN" to MaterialTheme.colorScheme.primary
        IssueStatus.IN_PROGRESS -> "IN PROGRESS" to MaterialTheme.colorScheme.tertiary
        IssueStatus.CLOSED -> "CLOSED" to MaterialTheme.colorScheme.secondary
        IssueStatus.CANCELLED -> "CANCELLED" to MaterialTheme.colorScheme.error
    }
    StatusPill(label = label, color = color, modifier = modifier)
}

@Composable
fun IssuePriorityPill(priority: IssuePriority, modifier: Modifier = Modifier) {
    val (label, color) = when (priority) {
        IssuePriority.HIGH -> "CAO" to MaterialTheme.colorScheme.error
        IssuePriority.MEDIUM -> "TB" to MaterialTheme.colorScheme.primary
        IssuePriority.LOW -> "THẤP" to MaterialTheme.colorScheme.secondary
    }
    StatusPill(label = label, color = color, modifier = modifier)
}
