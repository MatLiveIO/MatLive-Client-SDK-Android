package com.matnsolutions.matlive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matnsolutions.matlive.lib.screens.AudioRoomScreen
import com.matnsolutions.matlive.lib.screens.HomeScreen
import com.matnsolutions.matlive.util.requestNeededPermissions
import com.matnsolutions.matlive_sdk.utils.kPrint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()
        kPrint(data = "Hello")
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            HomeScreen(onNavigateToAudioRoom = { roomId, token ->
                navController.navigate("audioRoom/$roomId?token=$token")
            })
        }
        composable(
            route = "audioRoom/{roomId}?token={token}",
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""
            AudioRoomScreen(roomId = roomId, token = token)
        }
    }
}
