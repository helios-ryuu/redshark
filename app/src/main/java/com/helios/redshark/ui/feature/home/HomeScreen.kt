package com.helios.redshark.ui.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.ui.feature.interaction.message.ConversationListScreen
import com.helios.redshark.ui.feature.interaction.notification.NotificationListScreen
import com.helios.redshark.ui.feature.interaction.notification.NotificationListViewModel
import com.helios.redshark.ui.home.HomeFeedScreen
import com.helios.redshark.ui.myideas.MyIdeasScreen
import java.util.UUID

private enum class HomeTab { FEED, IDEAS, MESSAGES, NOTIFICATIONS }

private fun HomeTab.title(): String = when (this) {
    HomeTab.FEED -> "Feed"
    HomeTab.IDEAS -> "Y tuong cua toi"
    HomeTab.MESSAGES -> "Messages"
    HomeTab.NOTIFICATIONS -> "Notifications"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUserId: String?,
    onNavigateToProfile: (userId: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToIdeaDetail: (UUID) -> Unit,
    onCreateIdea: () -> Unit,
    onIssueClick: (UUID) -> Unit,
    onOpenConversation: (String, String?) -> Unit,
    onCreateConversation: (String) -> Unit,
    notificationListViewModel: NotificationListViewModel = hiltViewModel(),
) {
    val notificationState by notificationListViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabName by rememberSaveable { mutableStateOf(HomeTab.FEED.name) }
    val selectedTab = runCatching { HomeTab.valueOf(selectedTabName) }.getOrDefault(HomeTab.FEED)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedTab.title()) },
                actions = {
                    IconButton(onClick = { currentUserId?.let(onNavigateToProfile) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                HomeNavItem(
                    selected = selectedTab == HomeTab.FEED,
                    onClick = { selectedTabName = HomeTab.FEED.name },
                    icon = Icons.Default.Home,
                    label = "Feed",
                )
                HomeNavItem(
                    selected = selectedTab == HomeTab.IDEAS,
                    onClick = { selectedTabName = HomeTab.IDEAS.name },
                    icon = Icons.Default.Lightbulb,
                    label = "Ideas",
                )
                HomeNavItem(
                    selected = selectedTab == HomeTab.MESSAGES,
                    onClick = { selectedTabName = HomeTab.MESSAGES.name },
                    icon = Icons.Default.Mail,
                    label = "Messages",
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.NOTIFICATIONS,
                    onClick = { selectedTabName = HomeTab.NOTIFICATIONS.name },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (notificationState.unreadCount > 0) {
                                    Badge { Text(notificationState.unreadCount.toString()) }
                                }
                            },
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    },
                    label = { Text("Notify") },
                )
            }
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (selectedTab) {
            HomeTab.FEED -> HomeFeedScreen(
                onIssueClick = onIssueClick,
                modifier = contentModifier,
            )
            HomeTab.IDEAS -> MyIdeasScreen(
                onIdeaClick = onNavigateToIdeaDetail,
                onCreateIdea = onCreateIdea,
                modifier = contentModifier,
            )
            HomeTab.MESSAGES -> ConversationListScreen(
                onOpenConversation = onOpenConversation,
                onCreateConversation = onCreateConversation,
                currentUserId = currentUserId,
                modifier = contentModifier,
            )
            HomeTab.NOTIFICATIONS -> NotificationListScreen(
                onOpenConversation = { conversationId -> onOpenConversation(conversationId, null) },
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun RowScope.HomeNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
    )
}

