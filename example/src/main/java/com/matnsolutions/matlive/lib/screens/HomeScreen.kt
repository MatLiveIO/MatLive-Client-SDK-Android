package com.matnsolutions.matlive.lib.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matnsolutions.matlive_sdk.services.LiveKitService
import com.matnsolutions.matlive_sdk.utils.kPrint
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToAudioRoom: (roomId: String, token: String, avatar: String, userName: String, userId: String) -> Unit) {
    val livekitService = remember { LiveKitService("https://tkstg.t-wasel.com") }
    var isCreateLoading by remember { mutableStateOf(false) }
    var isJoinLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
            verticalArrangement = Arrangement.Center
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
            val displayMessage =
                if (roomId != null) roomId!! else "Create a new room and start talking with others"
            Text(
                text = displayMessage,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.Gray,
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Button(
                    onClick = {
                        if (!isCreateLoading) {
                            coroutineScope.launch {
                                createRoom(livekitService) { isCreateLoading = it }
                            }
                        }
                    },
                    enabled = !isCreateLoading,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isCreateLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Room",
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier.size(width = 300.dp, height = 50.dp)
            ) {
                Button(
                    onClick = {
                        if (!isJoinLoading) {
                            coroutineScope.launch {
                                joinRoom(
                                    livekitService,
                                    { isJoinLoading = it },
                                    roomId!!
                                ) { roomId, token ->
//                                    val intent = Intent(
//                                        this@MainActivity,
//                                        AudioRoomScreen::class.java
//                                    )
//                                    intent.putExtra("roomId", roomId)
//                                    intent.putExtra("token", token)
//                                    startActivity(intent)
                                    onNavigateToAudioRoom(
                                        roomId,
                                        token,
                                        "https://img-cdn.pixlr.com/image-generator/history/6565c8dff9ef18d69df3e3a2/fe1887b5-015e-4421-8c6a-1364d2f5b1e9/medium.webp",
                                        "Ahmed Attia",
                                        "10"
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isJoinLoading,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isJoinLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Join Room as 'Ahmed Attia'",
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(width = 300.dp, height = 50.dp)
            ) {
                Button(
                    onClick = {
                        if (!isJoinLoading) {
                            coroutineScope.launch {
                                joinRoom(
                                    livekitService,
                                    { isJoinLoading = it },
                                    roomId!!
                                ) { roomId, token ->
//                                    val intent = Intent(
//                                        this@MainActivity,
//                                        AudioRoomScreen::class.java
//                                    )
//                                    intent.putExtra("roomId", roomId)
//                                    intent.putExtra("token", token)
//                                    startActivity(intent)
                                    onNavigateToAudioRoom(
                                        roomId,
                                        token,
                                        "https://img-cdn.pixlr.com/image-generator/history/65772796905f29530816ea40/4ca9ba3d-c418-4153-a36a-77f4182236a7/medium.webp",
                                        "Test User2",
                                        "12"
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isJoinLoading,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isJoinLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Join Room as 'Test User2'",
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                }
            }
        }
    }
}

var roomId: String? = "test_room"
private suspend fun createRoom(
    livekitService: LiveKitService,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    try {
        val roomResponse = livekitService.createRoom("test_room")
        roomResponse.onSuccess { response ->
            // Handle success
            roomId = (response["data"] as Map<*, *>)["sid"] as String
            println("Room created: id $roomId")
        }.onFailure { error ->
            // Handle error
            println("Error: ${error.message}")
        }
    } catch (e: Exception) {
        kPrint(data = "Error: $e")
    } finally {
        setLoading(false)
    }
}

private suspend fun joinRoom(
    livekitService: LiveKitService,
    setLoading: (Boolean) -> Unit,
    roomId: String,
    onSuccess: (String, String) -> Unit
) {
    setLoading(true)
    try {
        val tokenResponse = livekitService.createToken(
            username = "user_${System.currentTimeMillis()}",
            roomId = roomId
        )
        tokenResponse.onSuccess { response ->
            // Handle success
            val token = response["data"] as String
            println("joinRoom: id $roomId, token $token")
            onSuccess(roomId, token)
        }.onFailure { error ->
            // Handle error
            println("Error: ${error.message}")
        }
    } catch (e: Exception) {
        kPrint(data = "Error: $e")
    } finally {
        setLoading(false)
    }
}
