package com.matnsolutions.matlive.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import com.matnsolutions.matlive_sdk.audio.mangers.MatLiveJoinRoomManger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoomScreen(
    roomId: String,
    avatar: String,
    userName: String,
    userId: String,
) {
    val viewModel: AudioRoomViewModel = viewModel()
    val controller = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.init(context, roomId,  userName, avatar, userId)
    }
    DisposableEffect(Unit) {
        onDispose {
            Log.e("DisposableEffect", "DisposableEffect")
//            CoroutineScope(Dispatchers.Main).launch {
//                MatLiveJoinRoomManger.instance.close()
//            }
        }
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
            if (viewModel.loading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                Column(modifier = Modifier.padding(vertical = 21.dp)) {
                    AudioRoomLayout(
                        viewModel = viewModel,
                    )

                    val messages by viewModel.messages.collectAsState()

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
                                    viewModel.sendMessage(controller.value)
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
