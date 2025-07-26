package com.lfgit.executors

/** Implement to invoke methods during execution. */
interface ExecListener {
    fun onExecStarted()
    fun onExecFinished(result: String, errCode: Int)
}