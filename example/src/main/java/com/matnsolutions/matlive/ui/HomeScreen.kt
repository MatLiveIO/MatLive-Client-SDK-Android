package com.matnsolutions.matlive.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToAudioRoom: (roomId: String, appKey: String, avatar: String, userName: String, userId: String) -> Unit) {
    var appKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var roomId by remember { mutableStateOf("room_2") }
    val isJoinLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        appKey = sharedPref.getString("app_key", "") ?: ""
        username = sharedPref.getString("username", "") ?: ""
        roomId = sharedPref.getString("room_id", "room_3") ?: "room_3"
    }

    val images = listOf(
        "https://img-cdn.pixlr.com/image-generator/history/65bb506dcb310754719cf81f/ede935de-1138-4f66-8ed7-44bd16efc709/medium.webp",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ718nztPNJfCbDJjZG8fOkejBnBAeQw5eAUA&s",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQll3t93lH9yx9shW9OMmDw5ft8sYdTs7bHcZZFyACGnKwdnWwPU7JW3KT2oAB0jEQSJiU&usqp=CAU",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTUXxSDbIbLLYxjHI9ht0lLf0VMmioBijVmoJeoItlMoUmfuu_AG3Or3K5kSx3YHbUBt3Q&usqp=CAU",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS1Dw7-4lVfRq74_YEiPEt4e-bQ0_6UA2y73Q&s",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-C_UAhXq9GfuGO452EEzfbKnh1viQB9EDBQ&s",
        "https://www.shutterstock.com/shutterstock/photos/2137527991/display_1500/stock-photo-portrait-of-smiling-mature-man-standing-on-white-background-2137527991.jpg",
        "https://images.pexels.com/photos/2379005/pexels-photo-2379005.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSplJ-5PtH61bgDfJtFiSWZtSOTjN_cyxamkg&s",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQqPRe6_8SSJ591Lt4jckiMaLvfvnjP2Z_oIQ&s",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSG7CH2bTx8kyDAU6Zc6rR0fX2X_4NGiANCTw&s"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MatLive Audio Rooms") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome to MatLive Audio Rooms",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = appKey,
                onValueChange = { appKey = it },
                label = { Text("App Key") },
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = roomId,
                onValueChange = { roomId = it },
                label = { Text("Room ID") },
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            val sharedPref = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }

            fun saveSettings() {
                with(sharedPref.edit()) {
                    putString("app_key", appKey)
                    putString("username", username)
                    putString("room_id", roomId)
                    apply()
                }
            }

            Box(
                modifier = Modifier.size(width = 300.dp, height = 50.dp)
            ) {
                Button(
                    onClick = {
                        if (!isJoinLoading) {
                            coroutineScope.launch {
                                onNavigateToAudioRoom(
                                    roomId,
                                    appKey,
                                    images[Random.nextInt(images.size)],
                                    username,
                                    Random.nextInt(1, 1001).toString()
                                )
                            }
                        }
                    },
                    enabled = !isJoinLoading && appKey.isNotEmpty() && username.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Join Room",
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(width = 300.dp, height = 50.dp)
            ) {
                Button(
                    onClick = { saveSettings() },
                    enabled = appKey.isNotEmpty() && username.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Save Settings",
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }
        }
    }

}

val images = listOf(
    "https://img-cdn.pixlr.com/image-generator/history/65bb506dcb310754719cf81f/ede935de-1138-4f66-8ed7-44bd16efc709/medium.webp",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ718nztPNJfCbDJjZG8fOkejBnBAeQw5eAUA&s",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQll3t93lH9yx9shW9OMmDw5ft8sYdTs7bHcZZFyACGnKwdnWwPU7JW3KT2oAB0jEQSJiU&usqp=CAU",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTUXxSDbIbLLYxjHI9ht0lLf0VMmioBijVmoJeoItlMoUmfuu_AG3Or3K5kSx3YHbUBt3Q&usqp=CAU",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS1Dw7-4lVfRq74_YEiPEt4e-bQ0_6UA2y73Q&s",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-C_UAhXq9GfuGO452EEzfbKnh1viQB9EDBQ&s",
    "https://www.shutterstock.com/shutterstock/photos/2137527991/display_1500/stock-photo-portrait-of-smiling-mature-man-standing-on-white-background-2137527991.jpg",
    "https://images.pexels.com/photos/2379005/pexels-photo-2379005.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSplJ-5PtH61bgDfJtFiSWZtSOTjN_cyxamkg&s",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQqPRe6_8SSJ591Lt4jckiMaLvfvnjP2Z_oIQ&s",
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSG7CH2bTx8kyDAU6Zc6rR0fX2X_4NGiANCTw&s",
