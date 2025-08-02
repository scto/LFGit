package com.lfgit.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lfgit.R
import com.lfgit.database.model.Repo
import com.lfgit.utilites.Constants
import com.lfgit.utilites.TaskState
import com.lfgit.view_models.Events.SingleLiveEvent
import org.apache.commons.lang3.StringUtils

/** Clone and Init repositories */
class AddRepoViewModel(application: Application) : ExecViewModel(application) {
    // data binding
    var initRepoPath: String? = null
    var cloneRepoPath: String? = null
    var cloneURLPath: String? = null

    val isShallowClone = MutableLiveData(false)
    var depth: String? = null

    private var mAllRepos: List<Repo>? = null
    val cloneResult = SingleLiveEvent<String>()
    val initResult = SingleLiveEvent<String>()

    val allRepos: LiveData<List<Repo>>
        get() = mRepository.allRepos

    fun setRepos(repoList: List<Repo>) {
        mAllRepos = repoList
    }

    private fun repoAlreadyAdded(path: String): Boolean {
        val cleanedPath = removeEndingForwardSlashes(path)
        if (mAllRepos?.any { it.localPath == cleanedPath } == true) {
            setShowToast(getAppString(R.string.repoAlreadyAdded))
            return true
        }
        return false
    }

    /** Handle a clone request */
    fun cloneRepoHandler() {
        if (StringUtils.isBlank(cloneRepoPath) || StringUtils.isBlank(cloneURLPath)) {
            setShowToast(getAppString(R.string.clone_prompt_info))
            return
        }
        if (!cloneURLPath.orEmpty().startsWith("https://") && !cloneURLPath.orEmpty().startsWith("http://")) {
            setShowToast(getAppString(R.string.clone_enter_remote))
            return
        }
        val fullRepoPath = getFullCloneRepoPath()
        if (repoAlreadyAdded(fullRepoPath)) return

        if (isShallowClone.value == true) {
            if (StringUtils.isBlank(depth)) {
                setShowToast(getAppString(R.string.clone_enter_depth))
                return
            }
            if (!ifNotWritableShowToast(cloneRepoPath!!)) return
            mState = TaskState(Constants.InnerState.FOR_USER, Constants.PendingTask.SHALLOW_CLONE)
            mGitExec.shallowClone(cloneRepoPath!!, cloneURLPath!!, depth!!)
        } else {
            if (!ifNotWritableShowToast(cloneRepoPath!!)) return
            mState = TaskState(Constants.InnerState.FOR_USER, Constants.PendingTask.CLONE)
            mGitExec.clone(cloneRepoPath!!, cloneURLPath!!)
        }
    }

    /** Handle the repository init request */
    fun initRepoHandler() {
        if (StringUtils.isBlank(initRepoPath)) {
            setShowToast(getAppString(R.string.init_enter_dir))
            return
        }
        if (repoAlreadyAdded(initRepoPath!!)) return
        if (!ifNotWritableShowToast(initRepoPath!!)) return
        mState = TaskState(Constants.InnerState.FOR_USER, Constants.PendingTask.INIT)
        mGitExec.init(initRepoPath!!)
    }

    private fun ifNotWritableShowToast(path: String): Boolean {
        Constants.mkdirsIfNotExist(path)
        if (!Constants.isWritablePath(path)) {
            setShowToast(getAppString(R.string.no_write_dir))
            return false
        }
        return true
    }

    /** Process the result of the execution of a binary file */
    fun processExecResult(execResult: ExecResult) {
        val result = execResult.result
        val errCode = execResult.errCode

        // Insert cloned or initialized repository to the database
        when (mState.pendingTask) {
            Constants.PendingTask.CLONE, Constants.PendingTask.SHALLOW_CLONE -> insertClonedRepo(result, errCode)
            Constants.PendingTask.INIT -> insertInitRepo(result, errCode)
            else -> {}
        }
    }

    /** Insert a cloned repository to the database */
    private fun insertClonedRepo(result: String, errCode: Int) {
        val successErrors = result.contains("Clone succeeded")
        if (errCode == 0 || successErrors) {
            val toastMsg = if (successErrors) result else getAppString(R.string.clone_success)

            val fullRepoPath = getFullCloneRepoPath()
            val url = removeEndingForwardSlashes(cloneURLPath.orEmpty())
            val repo = Repo(localPath = fullRepoPath, remoteURL = url)
            mRepository.insertRepo(repo)
            cloneResult.value = toastMsg
        } else {
            setShowToast(result)
        }
    }

    /** Insert an initialized repository to the database */
    private fun insertInitRepo(result: String, errCode: Int) {
        if (errCode == 0) {
            val path = removeEndingForwardSlashes(initRepoPath.orEmpty())
            mRepository.insertRepo(Repo(localPath = path, remoteURL = getAppString(R.string.local_repo)))
            initResult.value = getAppString(R.string.init_success)
        } else {
            setShowToast(result)
        }
    }

    /** Returns path without ending forward slashes */
    private fun removeEndingForwardSlashes(path: String): String {
        return path.replace("/+$".toRegex(), "")
    }

    private fun getFullCloneRepoPath(): String {
        val path = removeEndingForwardSlashes(cloneRepoPath.orEmpty())
        val url = removeEndingForwardSlashes(cloneURLPath.orEmpty())
        return "$path/${Constants.getGitDir(url)}"
    }
} 