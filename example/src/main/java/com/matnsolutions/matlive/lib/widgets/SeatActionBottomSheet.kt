package com.matnsolutions.matlive.lib.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatActionBottomSheet(
    showBottomSheet: Boolean,
    user: MatLiveUser?,
    onTakeMic: (() -> Unit)? = null,
    onMuteMic: (() -> Unit)? = null,
    onRemoveSpeaker: (() -> Unit)? = null,
    onLeaveMic: (() -> Unit)? = null,
    onLockMic: (() -> Unit)? = null,
    onUnLockMic: (() -> Unit)? = null,
    onSwitch: (() -> Unit)? = null,
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
                if (user == null) {
                    ActionButton(
                        icon = Icons.Filled.Edit,
                        label = "Take Mic",
                        onTap = {
                            onTakeMic?.invoke()
                            onDismiss()
                        }
                    )
                    ActionButton(
                        icon = Icons.Filled.Info,
                        label = "Switch Mic",
                        onTap = {
                            onSwitch?.invoke()
                            onDismiss()
                        },
                        isDestructive = true
                    )
                    ActionButton(
                        icon = Icons.Filled.Lock,
                        label = "Lock Mic",
                        onTap = {
                            onLockMic?.invoke()
                            onDismiss()
                        },
                        isDestructive = true
                    )
                    ActionButton(
                        icon = Icons.Filled.AccountBox,
                        label = "UnLock Mic",
                        onTap = {
                            onUnLockMic?.invoke()
                            onDismiss()
                        },
                        isDestructive = true
                    )
                } else {
                    val isMicOn by user.isMicOnNotifier.observeAsState(initial = false)
                    if (isMicOn) {
                        ActionButton(
                            icon = Icons.Filled.MicOff,
                            label = "Mute Mic",
                            onTap = {
                                onMuteMic?.invoke()
                                onDismiss()
                            }
                        )
                    } else {
                        ActionButton(
                            icon = Icons.Filled.Mic,
                            label = "UnMute Mic",
                            onTap = {
                                onMuteMic?.invoke()
                                onDismiss()
                            }
                        )
                    }
                    ActionButton(
                        icon = Icons.Filled.Delete,
                        label = "Remove Speaker",
                        onTap = {
                            onRemoveSpeaker?.invoke()
                            onDismiss()
                        },
                        isDestructive = true
                    )
                    ActionButton(
                        icon = Icons.Filled.ExitToApp,
                        label = "Leave Mic",
                        onTap = {
                            onLeaveMic?.invoke()
                            onDismiss()
                        },
                        isDestructive = true
                    )
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