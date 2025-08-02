package com.lfgit.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lfgit.database.RepoRepository
import com.lfgit.executors.ExecListener
import com.lfgit.executors.GitExec
import com.lfgit.executors.GitExecListener
import com.lfgit.utilites.Constants
import com.lfgit.utilites.TaskState
import com.lfgit.view_models.Events.SingleLiveEvent

/** A common ViewModel for other ViewModels handling binary files execution */
abstract class ExecViewModel(application: Application) : AndroidViewModel(application),
    ExecListener, GitExecListener {

    /** Result wrapper */
    data class ExecResult(var result: String, var errCode: Int)

    @JvmField
    var mApplication: Application = application
    @JvmField
    var mGitExec: GitExec
    @JvmField
    var mRepository: RepoRepository
    @JvmField
    var mState: TaskState

    val execResult = SingleLiveEvent<ExecResult>()
    val showToast = SingleLiveEvent<String>()
    val execPending = SingleLiveEvent<Boolean>()

    init {
        mRepository = RepoRepository(application)
        mGitExec = GitExec(this, this, application)
        mState = TaskState(Constants.InnerState.FOR_APP, Constants.PendingTask.NONE)
    }

    fun getAppString(resId: Int): String {
        return mApplication.getString(resId)
    }

    // background thread
    override fun onExecStarted() {
        showPendingIfNeeded(mState)
    }

    // background thread
    override fun onExecFinished(result: String, errCode: Int) {
        hidePendingIfNeeded(mState)
        postExecResult(ExecResult(result, errCode))
    }

    override fun onError(errorMsg: String) {
        setShowToast(errorMsg)
    }

    /** Check if task is long running */
    private fun isLongTask(pendingTask: Constants.PendingTask): Boolean {
        return pendingTask in listOf(
            Constants.PendingTask.PUSH,
            Constants.PendingTask.PULL,
            Constants.PendingTask.CLONE,
            Constants.PendingTask.SHALLOW_CLONE,
            Constants.PendingTask.CHECKOUT_REMOTE,
            Constants.PendingTask.CHECKOUT_LOCAL
        )
    }

    /** Check if long running task is finished */
    private fun longUserTaskFinished(state: TaskState): Boolean {
        val currentPendingTask = state.pendingTask
        return state.innerState == Constants.InnerState.FOR_USER && isLongTask(currentPendingTask)
    }

    // background thread
    fun showPendingIfNeeded(state: TaskState) {
        if (longUserTaskFinished(state)) postShowPending()
    }

    // background thread
    fun hidePendingIfNeeded(state: TaskState) {
        if (longUserTaskFinished(state)) postHidePending()
    }

    // background thread
    fun postShowPending() {
        execPending.postValue(true)
    }

    // background thread
    fun postHidePending() {
        execPending.postValue(false)
    }

    fun setShowToast(message: String) {
        showToast.value = message
    }

    fun postShowToast(message: String) {
        showToast.postValue(message)
    }

    fun postExecResult(result: ExecResult) {
        execResult.postValue(result)
    }
}