package com.helios.redshark.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightbulbCircle
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.auth.AuthViewModel
import com.helios.redshark.ui.message.ConversationListScreen
import com.helios.redshark.ui.myideas.MyIdeasScreen
import com.helios.redshark.ui.notification.NotificationListScreen
import com.helios.redshark.ui.notification.NotificationViewModel
import com.helios.redshark.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.util.UUID

private enum class HomeTab { HOME, IDEAS, NOTIFICATIONS, MESSAGES }

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
    val notifState by notificationViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var barsVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                barsVisible = available.y >= 0f
                return Offset.Zero
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(Dimens.SpaceLg),
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.home_drawer_settings)) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            topBar = {
                AnimatedVisibility(
                    visible = barsVisible,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                ) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.home_title_app)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.home_action_open_menu),
                                )
                            }
                        },
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = barsVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.HOME,
                            onClick = { selectedTab = HomeTab.HOME },
                            icon = {
                                Icon(
                                    if (selectedTab == HomeTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(R.string.home_tab_home)) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.IDEAS,
                            onClick = { selectedTab = HomeTab.IDEAS },
                            icon = {
                                Icon(
                                    if (selectedTab == HomeTab.IDEAS) Icons.Default.Lightbulb else Icons.Outlined.LightbulbCircle,
                                    contentDescription = null,
                                )
                            },
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
                                    Icon(
                                        if (selectedTab == HomeTab.NOTIFICATIONS) Icons.Default.Notifications else Icons.Outlined.NotificationsNone,
                                        contentDescription = null,
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.home_tab_notifications)) },
                        )
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.MESSAGES,
                            onClick = { selectedTab = HomeTab.MESSAGES },
                            icon = {
                                Icon(
                                    if (selectedTab == HomeTab.MESSAGES) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(R.string.home_tab_messages)) },
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { onNavigateToProfile(currentUserId ?: "") },
                            icon = {
                                Icon(
                                    if (false) Icons.Default.Person else Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(R.string.home_tab_profile)) },
                        )
                    }
                }
            },
        ) { padding ->
            when (selectedTab) {
                HomeTab.HOME -> HomeFeedScreen(
                    onIdeaClick = onNavigateToIdeaDetail,
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
}
