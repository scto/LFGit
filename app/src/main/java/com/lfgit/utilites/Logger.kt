package com.lfgit.utilites

import android.util.Log
import com.lfgit.BuildConfig

object Logger {
    fun LogDebugMsg(msg: String?) {
        val tag = "LFGit_Debug"
        if (BuildConfig.DEBUG) {
            val logMsg = when {
                msg == null -> "null"
                msg.isEmpty() -> "EMPTY MSG"
                else -> msg
            }
            Log.d(tag, logMsg)
        }
    }
}