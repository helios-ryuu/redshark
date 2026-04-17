package com.helios.redshark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.ui.navigation.NavGraph
import com.helios.redshark.ui.navigation.Routes
import com.helios.redshark.ui.theme.RedSharkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startDestination = if (firebaseAuth.currentUser != null) {
            Routes.HOME
        } else {
            Routes.AUTH_GOOGLE
        }
        setContent {
            RedSharkTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }
}
