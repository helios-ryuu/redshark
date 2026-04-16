package com.helios.redshark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.helios.redshark.ui.feature.auth.GoogleSignInScreen
import com.helios.redshark.ui.feature.auth.ProfileSetupScreen
import com.helios.redshark.ui.feature.home.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.AUTH_GOOGLE,
) {
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
                onSignOut = {
                    navController.navigate(Routes.AUTH_GOOGLE) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}
