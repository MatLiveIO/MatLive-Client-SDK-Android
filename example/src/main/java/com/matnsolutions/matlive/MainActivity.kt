package com.matnsolutions.matlive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matnsolutions.matlive.ui.AudioRoomScreen
import com.matnsolutions.matlive.ui.HomeScreen
import com.matnsolutions.matlive.ui.theme.MatliveTheme
import com.matnsolutions.matlive.util.requestNeededPermissions

data class Message(val id: Int, val text: String, val isUser: Boolean)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()
        enableEdgeToEdge()
        setContent {
            MatliveTheme {
                AppNavigation()
            }
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
