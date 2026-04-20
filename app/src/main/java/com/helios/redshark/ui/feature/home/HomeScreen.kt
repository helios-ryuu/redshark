package com.helios.redshark.ui.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.ui.feature.auth.AuthViewModel
import com.helios.redshark.ui.home.HomeFeedScreen
import com.helios.redshark.ui.myideas.MyIdeasScreen
import java.util.UUID

private enum class HomeTab { FEED, IDEAS }

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
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(HomeTab.FEED) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == HomeTab.FEED) "Feed" else "Ý tưởng của tôi") },
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
                NavigationBarItem(
                    selected = selectedTab == HomeTab.FEED,
                    onClick = { selectedTab = HomeTab.FEED },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.IDEAS,
                    onClick = { selectedTab = HomeTab.IDEAS },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Ideas") },
                    label = { Text("Ideas") },
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
        }
    }
}
