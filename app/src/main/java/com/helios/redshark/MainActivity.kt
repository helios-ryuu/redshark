package com.helios.redshark

// File nay giu logic chinh cua thanh phan nay trong ung dung.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.helios.redshark.ui.navigation.NavGraph
import com.helios.redshark.ui.theme.RedSharkTheme
import dagger.hilt.android.AndroidEntryPoint

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedSharkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
