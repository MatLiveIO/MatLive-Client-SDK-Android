package com.matnsolutions.matlive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnsolutions.matlive_sdk.audio.define.MatLiveRoomAudioSeat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatActionBottomSheet(
    showBottomSheet: Boolean,
    seat: MatLiveRoomAudioSeat,
    seatIndex: Int,
    audioRoomViewModel: AudioRoomViewModel,
    onDismiss: () -> Unit,
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (seat.currentUser.value == null) {
                    if (!audioRoomViewModel.onMic && !seat.isLocked.value!!) {
                        ActionButton(
                            icon = Icons.Filled.Mic,
                            label = "Take Mic",
                            onTap = {
                                audioRoomViewModel.takeSeat(seatIndex)
                                onDismiss()
                            }
                        )
                    }
                    if (audioRoomViewModel.onMic && !seat.isLocked.value!!) {
                        ActionButton(
                            icon = Icons.Filled.SwapCalls,
                            label = "Switch Mic",
                            onTap = {
                                audioRoomViewModel.switchSeat(seatIndex)
                                onDismiss()
                            },
                            isDestructive = true
                        )
                    }
                    if (!seat.isLocked.value!!) {
                        ActionButton(
                            icon = Icons.Filled.Lock,
                            label = "Lock Mic",
                            onTap = {
                                audioRoomViewModel.lockSeat(seatIndex)
                                onDismiss()
                            },
                            isDestructive = true
                        )
                    } else {
                        ActionButton(
                            icon = Icons.Filled.LockOpen,
                            label = "Unlock Mic",
                            onTap = {
                                audioRoomViewModel.unLockSeat(seatIndex)
                                onDismiss()
                            },
                            isDestructive = true
                        )
                    }
                } else {
                    val currentUserId = audioRoomViewModel.currentUser?.userId
                    val user = seat.currentUser.value!!
                    val isMicOn by user.isMicOnNotifier.observeAsState(initial = false)
                    if (isMicOn) {
                        ActionButton(
                            icon = Icons.Filled.MicOff,
                            label = "Mute Mic",
                            onTap = {
                                audioRoomViewModel.muteSeat(seatIndex)
                                onDismiss()
                            }
                        )
                    } else {
                        ActionButton(
                            icon = Icons.Filled.Mic,
                            label = "UnMute Mic",
                            onTap = {
                                audioRoomViewModel.unMuteSeat(seatIndex)
                                onDismiss()
                            }
                        )
                    }
                    if (seat.currentUser.value?.userId == currentUserId) {
                        ActionButton(
                            icon = Icons.Filled.ExitToApp,
                            label = "Leave Mic",
                            onTap = {
                                audioRoomViewModel.leaveSeat(seatIndex)
                                onDismiss()
                            },
                            isDestructive = true
                        )
                    } else {
                        ActionButton(
                            icon = Icons.Filled.PersonRemove,
                            label = "Remove Speaker",
                            onTap = {
                                audioRoomViewModel.removeUserFromSeat(seatIndex)
                                onDismiss()
                            },
                            isDestructive = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onTap: (() -> Unit)?,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onTap != null) { onTap?.invoke() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDestructive) Color.Red else Color.Black.copy(alpha = 0.87f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 16.sp,
                color = if (isDestructive) Color.Red else Color.Black.copy(alpha = 0.87f),
            )
        )
    }
}
