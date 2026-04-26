package com.helios.redshark.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.auth.AuthViewModel
import com.helios.redshark.ui.home.HomeFeedScreen
import com.helios.redshark.ui.message.ConversationListScreen
import com.helios.redshark.ui.myideas.MyIdeasScreen
import com.helios.redshark.ui.notification.NotificationListScreen
import com.helios.redshark.ui.notification.NotificationViewModel
import java.util.UUID

private enum class HomeTab { FEED, IDEAS, NOTIFICATIONS, MESSAGES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUserId: String?,
    onNavigateToProfile: (userId: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToIdeaDetail: (UUID) -> Unit,
    onCreateIdea: () -> Unit,
    onIssueClick: (UUID) -> Unit,
    onOpenConversation: (UUID) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val notifState by notificationViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(HomeTab.FEED) }

    val tabTitle = when (selectedTab) {
        HomeTab.FEED -> stringResource(R.string.home_title_feed)
        HomeTab.IDEAS -> stringResource(R.string.home_title_ideas)
        HomeTab.NOTIFICATIONS -> stringResource(R.string.home_title_notifications)
        HomeTab.MESSAGES -> stringResource(R.string.home_title_messages)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabTitle) },
                actions = {
                    IconButton(onClick = { currentUserId?.let(onNavigateToProfile) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.home_action_profile))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.home_action_settings))
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.FEED,
                    onClick = { selectedTab = HomeTab.FEED },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.home_tab_feed)) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.IDEAS,
                    onClick = { selectedTab = HomeTab.IDEAS },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    label = { Text(stringResource(R.string.home_tab_ideas)) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.NOTIFICATIONS,
                    onClick = { selectedTab = HomeTab.NOTIFICATIONS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (notifState.unreadCount > 0) {
                                    Badge { Text(notifState.unreadCount.coerceAtMost(99).toString()) }
                                }
                            },
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                    },
                    label = { Text(stringResource(R.string.home_tab_notifications)) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.MESSAGES,
                    onClick = { selectedTab = HomeTab.MESSAGES },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                    label = { Text(stringResource(R.string.home_tab_messages)) },
                )
            }
        },
    ) { padding ->
        when (selectedTab) {
            HomeTab.FEED -> HomeFeedScreen(
                onIssueClick = onIssueClick,
                modifier = Modifier.padding(padding),
            )
            HomeTab.IDEAS -> MyIdeasScreen(
                onIdeaClick = onNavigateToIdeaDetail,
                onCreateIdea = onCreateIdea,
                modifier = Modifier.padding(padding),
            )
            HomeTab.NOTIFICATIONS -> NotificationListScreen(
                modifier = Modifier.padding(padding),
                viewModel = notificationViewModel,
            )
            HomeTab.MESSAGES -> ConversationListScreen(
                currentUserId = currentUserId,
                onOpenConversation = onOpenConversation,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
