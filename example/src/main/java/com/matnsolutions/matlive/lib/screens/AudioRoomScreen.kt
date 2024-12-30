package com.matnsolutions.matlive.lib.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matnsolutions.matlive.lib.widgets.AudioRoomLayout
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveJoinRoomManger
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveRoomManger
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutConfig
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutRowConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoomScreen(roomId: String, token: String) {
    val matLiveRoomManger = MatLiveRoomManger.instance
    var loading by remember { mutableStateOf(true) }
    val controller = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        MatLiveJoinRoomManger.instance.init(
            onInvitedToMic = {},
            onSendGift = {}
        )
        MatLiveJoinRoomManger.instance.connect(
            context = context,
            roomId = roomId,
            token = token,
            name = "Ahmed Attia",
            avatar = "https://img-cdn.pixlr.com/image-generator/history/6565c8dff9ef18d69df3e3a2/fe1887b5-015e-4421-8c6a-1364d2f5b1e9/medium.webp",
            userId = "10",
            metadata = "{userRome:\"admin\"}"
        )

        val seatService = MatLiveRoomManger.instance.seatService
        seatService?.initWithConfig(
            MatLiveAudioRoomLayoutConfig(
                rowSpacing = 16.0,
                rowConfigs = listOf(
                    MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12),
                    MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12)
                )
            )
        )

        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = "Room: $roomId",
                    style = TextStyle(color = Color.Black, fontSize = 16.sp)
                )
            })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                Column(modifier = Modifier.padding(vertical = 21.dp)) {
                    AudioRoomLayout(
                        onTakeMic = { scope.launch { matLiveRoomManger.takeSeat(it) } },
                        onMuteMic = { scope.launch { matLiveRoomManger.muteSeat(it) } },
                        onRemoveSpeaker = { scope.launch { matLiveRoomManger.removeUserFromSeat(it) } },
                        onLeaveMic = { scope.launch { matLiveRoomManger.leaveSeat(it) } },
                        onSwitchSeat = { scope.launch { matLiveRoomManger.switchSeat(it) } },
                        onLockMic = { scope.launch { matLiveRoomManger.lockSeat(it) } },
                        onUnLockMic = { scope.launch { matLiveRoomManger.unLockSeat(it) } }
                    )

                    val messages by matLiveRoomManger.messages.collectAsState()

                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 21.dp),
                        ) {
                            items(messages) { item ->
                                ChatMessageItem(item = item)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = controller.value,
                            onValueChange = { controller.value = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message ...") },
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.White,
                            ),
                        )
                        IconButton(onClick = {
                            if (controller.value.isNotEmpty()) {
                                scope.launch {
                                    matLiveRoomManger.sendMessage(controller.value)
                                    controller.value = ""
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ChatMessageItem(item: MatLiveChatMessage) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.user.avatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
//                error = {
//                    Icon(Icons.Filled.Person, contentDescription = "User Avatar", modifier = Modifier.size(22.dp))
//                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item.user.name)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = item.message)
    }
}
