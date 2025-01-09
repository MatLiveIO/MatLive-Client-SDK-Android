# MatLive SDK Documentation

## Overview
The MatLive SDK provides audio room management capabilities including:
- Room connection management
- Audio track handling
- Seat management
- User management
- Chat messaging
- Event handling

## Installation
Add the SDK dependency to your project.

## Usage

### Initialization
Initialize the SDK in your ViewModel:

```kotlin
val matLiveRoomManger = MatLiveRoomManger.instance

fun init(
    context: Context,
    roomId: String, 
    appKey: String,
    userName: String,
    avatar: String,
    userId: String
) {
    viewModelScope.launch {
        matLiveRoomManger.init(
            onInvitedToMic = {},
            onSendGift = {}
        )
        
        matLiveRoomManger.connect(
            appKey = appKey,
            context = context,
            roomId = roomId,
            name = userName,
            avatar = avatar,
            userId = userId
        )
        
        // Configure seat layout
        matLiveRoomManger.seatService.initWithConfig(
            MatLiveAudioRoomLayoutConfig(
                rowSpacing = 16.0,
                rowConfigs = listOf(
                    MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12),
                    MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12)
                )
            )
        )
    }
}
```

### Seat Management
```kotlin
// Take a seat
fun takeSeat(seatIndex: Int) {
    viewModelScope.launch {
        matLiveRoomManger.takeSeat(seatIndex)
    }
}

// Leave a seat
fun leaveSeat(seatIndex: Int) {
    viewModelScope.launch {
        matLiveRoomManger.leaveSeat(seatIndex)
    }
}

// Lock/unlock seats
fun lockSeat(seatIndex: Int) {
    viewModelScope.launch {
        matLiveRoomManger.lockSeat(seatIndex)
    }
}

fun unLockSeat(seatIndex: Int) {
    viewModelScope.launch {
        matLiveRoomManger.unLockSeat(seatIndex)
    }
}
```

### Audio Controls
```kotlin
// Mute/unmute
fun muteSeat(seatIndex: Int) {
    viewModelScope.launch {
        matLiveRoomManger.muteSeat(seatIndex)
    }
}

fun unMuteSeat(seatIndex: Int) {
    viewModelScope.launch {
        matLiveRoomManger.unMuteSeat(seatIndex)
    }
}
```

### Messaging
```kotlin
fun sendMessage(message: String) {
    viewModelScope.launch {
        matLiveRoomManger.sendMessage(message)
    }
}
```

### Cleanup
```kotlin
override fun onCleared() {
    super.onCleared()
    MatLiveRoomManger.instance.close()
}
```

## Configuration
Customize the room layout using `MatLiveAudioRoomLayoutConfig`:

```kotlin
MatLiveAudioRoomLayoutConfig(
    rowSpacing = 16.0,
    rowConfigs = listOf(
        MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12),
        MatLiveAudioRoomLayoutRowConfig(count = 4, seatSpacing = 12)
    )
)
```

## Requirements
- Android API level 21+
- Kotlin Coroutines
- LiveKit Android SDK
