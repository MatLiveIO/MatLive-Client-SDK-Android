package com.matnsolutions.matlive_sdk.utils

import android.util.Log
import io.livekit.android.BuildConfig

fun kPrint(data: Any?) {
//    if (BuildConfig.DEBUG) {
        when (data) {
            is String -> _pr(data)
            is Map<*, *> -> _pr(data.toString()) // Convert Map to String
            else -> _pr(data.toString())
        }
//    }
}

private fun _pr(data: String) {
    Log.d("KPrint", data)
//    Log.d("KPrint", Thread.currentThread().stackTrace[2].toString())
}