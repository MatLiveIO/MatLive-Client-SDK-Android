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
import com.matnsolutions.matlive.ui.AudioRoomScreen
import com.matnsolutions.matlive.ui.HomeScreen
import com.matnsolutions.matlive.util.requestNeededPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()
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
            HomeScreen(onNavigateToAudioRoom = { roomId, appKey, avatar, userName, userId ->
                navController.navigate("audioRoom/$roomId?appKey=$appKey&avatar=$avatar&userName=$userName&userId=$userId")
            })
        }
        composable(
            route = "audioRoom/{roomId}?appKey={appKey}&avatar={avatar}&userName={userName}&userId={userId}",
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("appKey") { type = NavType.StringType },
                navArgument("avatar") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val appKey = backStackEntry.arguments?.getString("appKey") ?: ""
            val avatar = backStackEntry.arguments?.getString("avatar") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AudioRoomScreen(
                roomId = roomId,
                appKey = appKey,
                avatar = avatar,
                userName = userName,
                userId = userId
            )
        }
    }
}
