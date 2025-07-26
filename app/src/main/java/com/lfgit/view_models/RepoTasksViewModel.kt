package com.lfgit.view_models

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.lfgit.R
import com.lfgit.database.model.Repo
import com.lfgit.executors.GitExecListener
import com.lfgit.utilites.Constants
import com.lfgit.utilites.Constants.InnerState.*
import com.lfgit.utilites.Constants.PendingTask.*
import com.lfgit.view_models.Events.SingleLiveEvent
import org.apache.commons.lang3.StringUtils
import java.util.*

/** Git tasks logic (Git task is a sequence ending with a Git command execution) */
class RepoTasksViewModel(application: Application) : ExecViewModel(application), GitExecListener {

    private var mRepo: Repo? = null
    val taskResult = MutableLiveData<String>()
    val noRepo = SingleLiveEvent<String>()
    val promptCredentials = SingleLiveEvent<Boolean>()
    val promptAddRemote = SingleLiveEvent<Boolean>()
    val promptCommit = SingleLiveEvent<Boolean>()
    val promptCheckout = SingleLiveEvent<Boolean>()
    val promptPattern = SingleLiveEvent<Boolean>()
    private var mTempRemoteURL: String? = null

    var branch = MutableLiveData<String>()

    private val tasks = arrayOf<() -> Unit>(
        ::gitAddAllToStage,
        ::gitCommit,
        ::gitPush,
        ::gitPull,
        ::gitStatus,
        ::gitLog,
        ::gitResetHard,
        ::gitAddRemote,
        ::gitSetRemote,
        ::gitListBranches,
        ::gitCheckoutLocal,
        ::gitCheckoutRemote,
        ::lfsTrackPattern,
        ::lfsUntrackPattern,
        ::lfsListPatterns,
        ::lfsListFiles,
        ::lfsPrune,
        ::lfsStatus,
        ::lfsEnv
    ) {
        mState.newState(FOR_USER, NONE)
        setPromptCredentials(true)
    }

    /** Execute a given Git task or show an error */
    fun execGitTask(drawerPosition: Int) {
        mRepo?.let {
            if (mRepository.repoDirExists(it)) {
                tasks[drawerPosition].invoke()
            } else {
                mRepository.deleteByID(it.id)
                noRepo.value = getAppString(R.string.repo_not_found)
            }
        }
    }

    private fun gitAddAllToStage() {
        mState.newState(FOR_USER, ADD)
        mGitExec.addAllToStage(repoPath)
    }

    private fun gitCommit() {
        mState.newState(FOR_APP, COMMIT)
        setPromptCommit(true)
    }

    private fun gitPush() {
        mState.newState(FOR_APP, PUSH)
        getRemoteGit()
    }

    private fun gitPull() {
        mState.newState(FOR_APP, PULL)
        getRemoteGit()
    }

    private fun gitStatus() {
        mState.newState(FOR_USER, STATUS)
        mGitExec.status(repoPath)
    }

    private fun gitLog() {
        mState.newState(FOR_USER, LOG)
        mGitExec.log(repoPath)
    }

    private fun gitResetHard() {
        mState.newState(FOR_USER, RESET_HARD)
        mGitExec.resetHard(repoPath)
    }

    private fun gitAddRemote() {
        mState.newState(FOR_APP, ADD_REMOTE)
        setPromptRemote(true)
    }

    private fun gitSetRemote() {
        mState.newState(FOR_APP, SET_REMOTE)
        setPromptRemote(true)
    }

    private fun gitListBranches() {
        mState.newState(FOR_USER, LIST_BRANCHES)
        mGitExec.listBranches(repoPath)
    }

    private fun gitCheckoutLocal() {
        mState.newState(FOR_USER, CHECKOUT_LOCAL)
        setPromptCheckout(true)
    }

    private fun gitCheckoutRemote() {
        mState.newState(FOR_USER, CHECKOUT_REMOTE)
        setPromptCheckout(true)
    }

    private fun lfsTrackPattern() {
        mState.newState(FOR_USER, LFS_TRACK)
        setPromptPattern(true)
    }

    private fun lfsUntrackPattern() {
        mState.newState(FOR_USER, LFS_UNTRACK)
        setPromptPattern(true)
    }

    private fun lfsListPatterns() {
        mState.newState(FOR_USER, LFS_LIST_PATTERNS)
        mGitExec.lfsListPatterns(repoPath)
    }

    private fun lfsListFiles() {
        mState.newState(FOR_USER, LFS_LIST_FILES)
        mGitExec.lfsListFiles(repoPath)
    }

    private fun lfsPrune() {
        mState.newState(FOR_USER, LFS_PRUNE)
        mGitExec.lfsPrune(repoPath)
    }

    private fun lfsStatus() {
        mState.newState(FOR_USER, LFS_STATUS)
        mGitExec.lfsStatus(repoPath)
    }

    private fun lfsEnv() {
        mState.newState(FOR_USER, LFS_ENV)
        mGitExec.lfsEnv(repoPath)
    }

    /*********************************************************************/

    private fun getRemoteGit() {
        mState.innerState = GET_REMOTE_GIT
        mRepo?.let { mGitExec.getRemoteURL(it) }
    }

    /** check if Git credentials are set in the database */
    private fun credentialsSetDB(): Boolean {
        return mRepo?.password != null && mRepo?.username != null
    }

    /** Handle new credentials */
    fun handleCredentials(username: String, password: String) {
        if (password.isNotBlank() && username.isNotBlank()) {
            setPromptCredentials(false)
            mRepo?.apply {
                this.username = username
                this.password = password
                mRepository.updateCredentials(this)
            }
            showToast.value = getAppString(R.string.credentials_saved)
            pushPendingAndFinish()
        } else {
            showToast.value = getAppString(R.string.enter_creds)
        }
    }

    private fun pushPendingAndFinish() {
        if (mState.pendingTask == PUSH) {
            mState.newState(FOR_USER, PUSH)
            mRepo?.let { mGitExec.push(it) }
        }
    }

    /** Handle a new remote URL */
    fun handleRemoteURL(remoteURL: String) {
        if (remoteURL.isNotBlank()) {
            val pendingTask = mState.pendingTask
            setPromptRemote(false)
            mTempRemoteURL = remoteURL

            mRepo?.let {
                when (pendingTask) {
                    ADD_REMOTE -> {
                        mState.innerState = ADD_ORIGIN_REMOTE
                        mGitExec.addOriginRemote(it, remoteURL)
                    }
                    SET_REMOTE -> {
                        mState.innerState = SET_ORIGIN_REMOTE
                        mGitExec.editOriginRemote(it, remoteURL)
                    }
                    else -> {}
                }
            }
        } else {
            showToast.value = getAppString(R.string.enter_remote)
        }
    }

    /** Handle a commit message */
    fun handleCommitMsg(message: String) {
        if (message.isNotBlank()) {
            setPromptCommit(false)
            mState.innerState = FOR_USER
            mGitExec.commit(repoPath, message)
        } else {
            showToast.value = getAppString(R.string.enter_commit_msg)
        }
    }

    /** Handle a branch to checkout */
    fun handleCheckoutBranch(branch: String) {
        if (branch.isNotBlank()) {
            setPromptCheckout(false)
            mState.innerState = FOR_USER
            if (mState.pendingTask == CHECKOUT_LOCAL) {
                mGitExec.checkoutLocal(repoPath, branch)
            } else {
                mGitExec.checkoutRemote(repoPath, branch)
            }
        } else {
            showToast.value = getAppString(R.string.enter_branch)
        }
    }

    /** Reset the viewModel task state */
    fun startState() {
        mState.newState(FOR_USER, NONE)
    }

    /** Handle a Git LFS pattern */
    fun handlePattern(pattern: String) {
        if (pattern.isNotBlank()) {
            setPromptPattern(false)
            mState.innerState = FOR_USER
            val pending = mState.pendingTask
            if (pending == LFS_TRACK) {
                mGitExec.lfsTrackPattern(repoPath, pattern)
            } else if (pending == LFS_UNTRACK) {
                mGitExec.lfsUntrackPattern(repoPath, pattern)
            }
        } else {
            showToast.value = getAppString(R.string.enter_pattern)
        }
    }

    /** Process the result of a command execution */
    fun processExecResult(execResult: ExecResult) {
        val result = execResult.result
        val errCode = execResult.errCode

        if (mState.innerState != FOR_USER) {
            processTaskResult(result, errCode)
        } else {
            if (result.isEmpty()) {
                showToast.value = if (errCode == 0) {
                    getAppString(R.string.operation_success)
                } else {
                    getAppString(R.string.operation_fail)
                }
            } else {
                taskResult.value = result
            }
            mState.newState(FOR_USER, NONE)
        }
    }

    /** Process a task result */
    private fun processTaskResult(result: String, errCode: Int) {
        val resultLines = result.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val innerState = mState.innerState
        if (innerState == GET_REMOTE_GIT) {
            if (resultLines.isEmpty() || errCode != 0) {
                mRepo?.apply {
                    remoteURL = getAppString(R.string.local_repo)
                    mRepository.updateRemoteURL(this)
                }
                showToast.value = getAppString(R.string.no_remote)
            } else {
                mRepo?.apply {
                    remoteURL = resultLines[0]
                    mRepository.updateRemoteURL(this)
                }
                if (mState.pendingTask == PULL) {
                    mState.newState(FOR_USER, PULL)
                    mRepo?.let { mGitExec.pull(it) }
                } else {
                    if (!credentialsSetDB()) {
                        setPromptCredentials(true)
                    } else {
                        pushPendingAndFinish()
                    }
                }
            }
        } else if (innerState == ADD_ORIGIN_REMOTE || innerState == SET_ORIGIN_REMOTE) {
            if (errCode != 0) {
                if (resultLines.isNotEmpty()) {
                    taskResult.value = result
                } else {
                    showToast.value = getAppString(R.string.operation_fail)
                }
            } else {
                mRepo?.apply { remoteURL = mTempRemoteURL }
                mRepo?.let { mRepository.updateRemoteURL(it) }
                showToast.value = if (innerState == ADD_ORIGIN_REMOTE) {
                    getAppString(R.string.origin_added)
                } else {
                    getAppString(R.string.origin_set)
                }
            }
            mState.newState(FOR_APP, NONE)
        }
    }

    fun setRepo(repo: Repo?) {
        mRepo = repo
    }

    private val repoPath: String
        get() = mRepo?.localPath ?: ""

    private fun setPromptCredentials(prompt: Boolean) {
        promptCredentials.value = prompt
    }

    fun setPromptRemote(prompt: Boolean) {
        promptAddRemote.value = prompt
    }

    fun setPromptCommit(prompt: Boolean) {
        promptCommit.value = prompt
    }

    fun setPromptPattern(prompt: Boolean) {
        promptPattern.value = prompt
    }

    fun setPromptCheckout(prompt: Boolean) {
        promptCheckout.value = prompt
    }
}