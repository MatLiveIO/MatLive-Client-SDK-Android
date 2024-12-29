package com.matnsolutions.matlive.lib.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.matnsolutions.matlive_sdk.audio.define.MatLiveUser

@Composable
fun SeatActionBottomSheet(
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .padding(bottom = 16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
                )

                if (user == null) {
                    ActionButton(icon = Icons.Filled.Edit, label = "Take Mic", onTap = onTakeMic)
                    ActionButton(icon = Icons.Filled.Info, label = "Switch Mic", onTap = onSwitch, isDestructive = true)
                    ActionButton(icon = Icons.Filled.Lock, label = "Lock Mic", onTap = onLockMic, isDestructive = true)
                    ActionButton(icon = Icons.Filled.AccountBox, label = "UnLock Mic", onTap = onUnLockMic, isDestructive = true)
                } else {
                    val isMicOn by user.isMicOnNotifier.observeAsState(initial = false)
                    if (isMicOn) {
                        ActionButton(icon = Icons.Filled.Close, label = "Mute Mic", onTap = onMuteMic)
                    } else {
                        ActionButton(icon = Icons.Filled.Check, label = "UnMute Mic", onTap = onMuteMic)
                    }
                    ActionButton(icon = Icons.Filled.Delete, label = "Remove Speaker", onTap = onRemoveSpeaker, isDestructive = true)
                    ActionButton(icon = Icons.Filled.ExitToApp, label = "Leave Mic", onTap = onLeaveMic, isDestructive = true)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@Composable
private fun Box(modifier: Modifier, shape: RoundedCornerShape = RoundedCornerShape(0.dp), color: Color = Color.Transparent, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        content = content
    )
}