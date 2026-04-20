package com.helios.redshark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.helios.redshark.ui.createidea.CreateIdeaScreen
import com.helios.redshark.ui.createissue.CreateIssueScreen
import com.helios.redshark.ui.editidea.EditIdeaScreen
import com.helios.redshark.ui.editissue.EditIssueScreen
import com.helios.redshark.ui.feature.auth.AuthViewModel
import com.helios.redshark.ui.feature.auth.GoogleSignInScreen
import com.helios.redshark.ui.feature.auth.ProfileSetupScreen
import com.helios.redshark.ui.feature.home.HomeScreen
import com.helios.redshark.ui.feature.profile.ProfileEditScreen
import com.helios.redshark.ui.feature.profile.ProfileViewScreen
import com.helios.redshark.ui.feature.settings.SettingsScreen
import com.helios.redshark.ui.ideadetail.IdeaDetailScreen
import com.helios.redshark.ui.issuedetail.IssueDetailScreen
import java.util.UUID

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.AUTH_GOOGLE,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId = authState.user?.id

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.AUTH_GOOGLE) {
            GoogleSignInScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_GOOGLE) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Routes.PROFILE_SETUP)
                },
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_GOOGLE) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                currentUserId = currentUserId,
                onNavigateToProfile = { userId -> navController.navigate(Routes.profileView(userId)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onSignOut = {
                    navController.navigate(Routes.AUTH_GOOGLE) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToIdeaDetail = { ideaId ->
                    navController.navigate(Routes.ideaDetail(ideaId.toString()))
                },
                onCreateIdea = { navController.navigate(Routes.IDEA_CREATE) },
                onIssueClick = { issueId ->
                    navController.navigate(Routes.issueDetail(issueId.toString()))
                },
            )
        }

        composable(Routes.IDEA_CREATE) {
            CreateIdeaScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreated = { ideaId ->
                    navController.navigate(Routes.ideaDetail(ideaId.toString())) {
                        popUpTo(Routes.IDEA_CREATE) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.IDEA_DETAIL,
            arguments = listOf(navArgument("ideaId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "redshark://idea/{ideaId}" }),
        ) { backStackEntry ->
            val ideaId = backStackEntry.arguments?.getString("ideaId")
                ?.let { UUID.fromString(it) } ?: return@composable
            IdeaDetailScreen(
                ideaId = ideaId,
                currentUserId = currentUserId ?: "",
                onNavigateBack = { navController.popBackStack() },
                onEditIdea = { navController.navigate(Routes.ideaEdit(it.toString())) },
                onCreateIssue = { navController.navigate(Routes.issueCreate(it.toString())) },
                onIssueClick = { navController.navigate(Routes.issueDetail(it.toString())) },
            )
        }

        composable(
            route = Routes.IDEA_EDIT,
            arguments = listOf(navArgument("ideaId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val ideaId = backStackEntry.arguments?.getString("ideaId")
                ?.let { UUID.fromString(it) } ?: return@composable
            EditIdeaScreen(
                ideaId = ideaId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ISSUE_CREATE,
            arguments = listOf(navArgument("ideaId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val ideaId = backStackEntry.arguments?.getString("ideaId")
                ?.let { UUID.fromString(it) } ?: return@composable
            CreateIssueScreen(
                ideaId = ideaId,
                currentUserId = currentUserId ?: "",
                onNavigateBack = { navController.popBackStack() },
                onCreated = { issueId ->
                    navController.navigate(Routes.issueDetail(issueId.toString())) {
                        popUpTo(Routes.issueCreate(ideaId.toString())) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.ISSUE_DETAIL,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId")
                ?.let { UUID.fromString(it) } ?: return@composable
            IssueDetailScreen(
                issueId = issueId,
                currentUserId = currentUserId ?: "",
                onNavigateBack = { navController.popBackStack() },
                onEditIssue = { navController.navigate(Routes.issueEdit(it.toString())) },
                onViewIdea = { navController.navigate(Routes.ideaDetail(it.toString())) },
            )
        }

        composable(
            route = Routes.ISSUE_EDIT,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId")
                ?.let { UUID.fromString(it) } ?: return@composable
            EditIssueScreen(
                issueId = issueId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.PROFILE_VIEW,
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            ProfileViewScreen(
                userId = userId,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Routes.PROFILE_EDIT) },
            )
        }

        composable(Routes.PROFILE_EDIT) {
            val userId = currentUserId ?: return@composable
            ProfileEditScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Routes.AUTH_GOOGLE) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToIdea = { ideaId ->
                    navController.navigate(Routes.ideaDetail(ideaId.toString()))
                },
            )
        }
    }
}
