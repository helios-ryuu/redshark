package com.helios.redshark.ui.home

import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.helios.redshark.ui.common.AvatarImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.ui.auth.AuthViewModel
import com.helios.redshark.ui.comment.CommentSheetContent
import com.helios.redshark.ui.message.ConversationListScreen
import com.helios.redshark.ui.message.MessageViewModel
import com.helios.redshark.ui.message.ShareConversationSheetContent
import com.helios.redshark.ui.navigation.Routes
import com.helios.redshark.ui.myideas.MyIdeasScreen
import com.helios.redshark.ui.notification.NotificationListScreen
import com.helios.redshark.ui.notification.NotificationViewModel
import com.helios.redshark.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.util.UUID

private enum class HomeTab { HOME, IDEAS, MESSAGES }

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
    onStartConversation: (String) -> Unit,
    onOpenConversation: (UUID) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val notifState by notificationViewModel.uiState.collectAsStateWithLifecycle()
    val messageState by messageViewModel.listState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.HOME) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNotifSheet by remember { mutableStateOf(false) }
    val notifSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCommentSheet by remember { mutableStateOf(false) }
    var commentIdeaId by remember { mutableStateOf<UUID?>(null) }
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShareSheet by remember { mutableStateOf(false) }
    var shareIdea by remember { mutableStateOf<Idea?>(null) }
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val messageUnreadCount = remember(messageState.conversations, currentUserId) {
        if (currentUserId == null) 0
        else messageState.conversations.count { it.hasUnread && it.lastMessageSenderId != currentUserId }
    }

    if (showNotifSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotifSheet = false },
            sheetState = notifSheetState,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_title_notifications),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(
                    onClick = notificationViewModel::deleteAll,
                    enabled = notifState.notifications.isNotEmpty(),
                ) {
                    Text(
                        text = stringResource(R.string.notification_action_delete_all),
                        style = MaterialTheme.typography.labelLarge.copy(
                            textDecoration = TextDecoration.Underline,
                        ),
                    )
                }
            }
            HorizontalDivider()
            Box(modifier = Modifier.fillMaxWidth().height(Dimens.NotificationSheetMaxHeight)) {
                NotificationListScreen(
                    viewModel = notificationViewModel,
                    onOpenIdea = onNavigateToIdeaDetail,
                )
            }
        }
    }

    if (showCommentSheet && commentIdeaId != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = commentSheetState,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.comment_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { showCommentSheet = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
            HorizontalDivider()
            Box(modifier = Modifier.fillMaxWidth().height(Dimens.NotificationSheetMaxHeight)) {
                CommentSheetContent(
                    ideaId = commentIdeaId!!,
                    currentUserId = currentUserId ?: "",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (showShareSheet && shareIdea != null) {
        val idea = shareIdea!!
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            sheetState = shareSheetState,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.message_share_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { showShareSheet = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
            HorizontalDivider()
            val description = idea.description?.trim().orEmpty()
            val shareLink = Routes.ideaDeepLink(idea.id.toString())
            val shareMessage = if (description.isNotEmpty()) {
                stringResource(R.string.message_share_idea_template, idea.title, description, shareLink)
            } else {
                stringResource(R.string.message_share_idea_template_title_only, idea.title, shareLink)
            }
            Box(modifier = Modifier.fillMaxWidth().height(Dimens.NotificationSheetMaxHeight)) {
                ShareConversationSheetContent(
                    currentUserId = currentUserId,
                    messageText = shareMessage,
                    onSent = { showShareSheet = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.DrawerHeaderHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                        .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceMd),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                    ) {
                        AvatarImage(
                            avatarUrl = authState.user?.avatarUrl,
                            displayName = authState.user?.displayName ?: stringResource(R.string.app_name),
                            size = Dimens.AvatarMd,
                        )
                        Column {
                            Text(
                                text = authState.user?.displayName ?: stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            )
                            authState.user?.email?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
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
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_redshark_no_text_large),
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconLg),
                            )
                            Text(stringResource(R.string.home_title_app))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.home_action_open_menu),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showNotifSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (notifState.unreadCount > 0) {
                                        Badge { Text(notifState.unreadCount.toString()) }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = if (notifState.unreadCount > 0)
                                        Icons.Default.Notifications
                                    else
                                        Icons.Outlined.NotificationsNone,
                                    contentDescription = stringResource(R.string.home_tab_notifications),
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
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
                        selected = selectedTab == HomeTab.MESSAGES,
                        onClick = { selectedTab = HomeTab.MESSAGES },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (messageUnreadCount > 0) {
                                        Badge { Text(messageUnreadCount.toString()) }
                                    }
                                },
                            ) {
                                Icon(
                                    if (selectedTab == HomeTab.MESSAGES) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = null,
                                )
                            }
                        },
                        label = { Text(stringResource(R.string.home_tab_messages)) },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateToProfile(currentUserId ?: "") },
                        icon = {
                            Icon(Icons.Outlined.PersonOutline, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.home_tab_profile)) },
                    )
                }
            },
        ) { padding ->
            when (selectedTab) {
                HomeTab.HOME -> HomeFeedScreen(
                    onIdeaClick = onNavigateToIdeaDetail,
                    onCommentClick = { ideaId ->
                        commentIdeaId = ideaId
                        showCommentSheet = true
                    },
                    onShareClick = { idea ->
                        shareIdea = idea
                        showShareSheet = true
                    },
                    modifier = Modifier.padding(padding),
                )
                HomeTab.IDEAS -> MyIdeasScreen(
                    onIdeaClick = onNavigateToIdeaDetail,
                    onCreateIdea = onCreateIdea,
                    onCommentClick = { ideaId ->
                        commentIdeaId = ideaId
                        showCommentSheet = true
                    },
                    onShareClick = { idea ->
                        shareIdea = idea
                        showShareSheet = true
                    },
                    modifier = Modifier.padding(padding),
                )
                HomeTab.MESSAGES -> ConversationListScreen(
                    currentUserId = currentUserId,
                    onOpenConversation = onOpenConversation,
                    onStartConversation = onStartConversation,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
