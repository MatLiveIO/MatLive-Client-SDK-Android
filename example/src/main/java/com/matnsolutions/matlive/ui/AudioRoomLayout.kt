package com.matnsolutions.matlive.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff

import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRoomAudioSeat
import com.matnsolutions.matlive_sdk.audio.seats.MatLiveAudioRoomLayoutConfig

@Composable
fun AudioRoomLayout(
    viewModel: AudioRoomViewModel,
) {
    val seatService = viewModel.matLiveRoomManger.seatService ?: return
    val seats by seatService.seatList.collectAsState()
    val layoutConfig = seatService.layoutConfig ?: return
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        layoutConfig.rowConfigs.forEachIndexed { rowIndex, rowConfig ->
            Row(
                modifier = Modifier.padding(bottom = layoutConfig.rowSpacing.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (seatIndex in 0 until rowConfig.count) {
                    val globalIndex = calculateGlobalIndex(rowIndex, seatIndex, layoutConfig)
                    val seat = seats.getOrNull(globalIndex)!!
                    SeatItem(
                        seat = seat,
                        seatIndex = globalIndex,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatItem(
    seat: MatLiveRoomAudioSeat,
    seatIndex: Int,
    viewModel: AudioRoomViewModel
) {
    val isLocked by seat.isLocked.observeAsState(initial = false)
    var showBottomSheet by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(horizontal = 6.dp)) {
        Surface(modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = Color.Gray.copy(alpha = 0.3f),
            onClick = {
                showBottomSheet = true
            }) {
            if (isLocked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Seat Locked",
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            } else {
                SeatWidget(seat = seat)
            }
        }
        if (showBottomSheet) {
            SeatActionBottomSheet(
                seat = seat,
                seatIndex = seatIndex,
                audioRoomViewModel = viewModel,
                showBottomSheet = showBottomSheet,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@Composable
private fun SeatWidget(seat: MatLiveRoomAudioSeat) {
    val user by seat.currentUser.observeAsState()
    Box(modifier = Modifier.size(50.dp)) {
        // User avatar
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = Color.Gray.copy(alpha = 0.3f)
        ) {
            if (user != null) {
                AsyncImage(
                    model = user?.avatarUrlNotifier?.value,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
//                    error = {
//                        Icon(Icons.Filled.Person, contentDescription = "User Avatar", modifier = Modifier.size(22.dp))
//                    }
                )
            } else {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Mic",
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
        }
        // Mic icon
        if (user != null) {
            val isMicOn by user!!.isMicOnNotifier.observeAsState(initial = false)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                ) {
                    Icon(
                        imageVector = if (isMicOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Mic Status",
                        modifier = Modifier.size(12.dp),
                        tint = if (isMicOn) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}


private fun calculateGlobalIndex(
    rowIndex: Int,
    seatIndex: Int,
    layoutConfig: MatLiveAudioRoomLayoutConfig
): Int {
    var totalPreviousSeats = 0
    for (i in 0 until rowIndex) {
        totalPreviousSeats += layoutConfig.rowConfigs[i].count
    }
    return totalPreviousSeats + seatIndex
}
