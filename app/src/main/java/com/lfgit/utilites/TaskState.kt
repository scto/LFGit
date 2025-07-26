package com.lfgit.utilites

import com.lfgit.utilites.Constants.InnerState
import com.lfgit.utilites.Constants.PendingTask

/** Task states enabling sequential processing of Git tasks */
data class TaskState(
    var innerState: InnerState,
    var pendingTask: PendingTask
) {
    fun newState(state: InnerState, task: PendingTask) {
        this.innerState = state
        this.pendingTask = task
    }
}