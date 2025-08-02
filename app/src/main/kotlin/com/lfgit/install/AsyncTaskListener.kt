package com.lfgit.install

interface AsyncTaskListener {
    fun onTaskStarted()
    fun onTaskFinished(installed: Boolean)
}