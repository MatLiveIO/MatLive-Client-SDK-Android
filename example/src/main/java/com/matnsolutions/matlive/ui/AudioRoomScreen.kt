package com.matnsolutions.matlive.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.matnsolutions.matlive_sdk.audio.define.MatLiveChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoomScreen(
    roomId: String,
    appKey: String,
    avatar: String,
    userName: String,
    userId: String,
) {
    val viewModel: AudioRoomViewModel = viewModel()
    val controller = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.init(context, roomId, appKey, userName, avatar, userId)
    }
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = "Room: $roomId",
            )
        })
    }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            if (viewModel.loading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {

                AudioRoomLayout(
                    viewModel = viewModel,
                )

                val messages by viewModel.messages.collectAsState()

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 21.dp)
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true
                ) {
                    items(messages) { item ->
                        ChatMessageItem(item = item)
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
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
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
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item.user.name)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = item.message)
    }
}
