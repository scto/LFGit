package com.lfgit.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import com.lfgit.R
import com.lfgit.database.model.Repo
import com.lfgit.utilites.Constants
import com.lfgit.utilites.TaskState
import java.util.*

/** Manage list of repositories logic */
class RepoListViewModel(application: Application) : ExecViewModel(application) {
    var allRepos: LiveData<List<Repo>> = mRepository.allRepos
        private set
    private var mAllRepos: List<Repo>? = null
    private var mLastRepo: Repo? = null
    var isInstalling = false

    fun setRepos(repoList: List<Repo>?) {
        mAllRepos = repoList
    }

    fun deleteRepoById(id: Int) {
        mRepository.deleteByID(id)
    }

    /** Initiate addition of a local repository */
    fun addLocalRepo(path: String) {
        var processedPath = path
        // check if a path is valid, strip .git suffix
        if (processedPath.endsWith("/.git")) {
            processedPath = processedPath.substring(0, processedPath.length - 5)
        } else if (processedPath.contains("/.git/")) {
            showToast.value = getAppString(R.string.not_a_git_repo)
            return
        }

        // check if repository is already added
        if (mAllRepos?.any { it.localPath == processedPath } == true) {
            showToast.value = getAppString(R.string.repoAlreadyAdded)
            return
        }

        // check if it is a Git repository
        mLastRepo = Repo(localPath = processedPath)
        mState = TaskState(Constants.InnerState.IS_REPO, Constants.PendingTask.NONE)
        mGitExec.isRepo(processedPath)
    }

    fun repoDirExists(repo: Repo): Boolean {
        return mRepository.repoDirExists(repo)
    }

    /** Process the execution result */
    fun processExecResult(execResult: ExecResult) {
        val errCode = execResult.errCode
        val result = execResult.result

        when (mState.innerState) {
            Constants.InnerState.IS_REPO -> {
                // checking for a Git repository
                if (errCode == 0) {
                    // get the repository remote URL
                    mState = TaskState(Constants.InnerState.GET_REMOTE_GIT, Constants.PendingTask.NONE)
                    mLastRepo?.let { mGitExec.getRemoteURL(it) }
                } else {
                    showToast.value = getAppString(R.string.not_a_git_repo)
                    mState = TaskState(Constants.InnerState.FOR_APP, Constants.PendingTask.NONE)
                }
            }
            Constants.InnerState.GET_REMOTE_GIT -> {
                // parse a list of remote URLs, set the remote as the first found
                val resultLines = result.split(System.lineSeparator()).filter { it.isNotEmpty() }
                mLastRepo?.let {
                    it.remoteURL = if (resultLines.isEmpty() || errCode != 0) {
                        getAppString(R.string.local_repo)
                    } else {
                        resultLines[0]
                    }
                    mRepository.insertRepo(it)
                }
                mState = TaskState(Constants.InnerState.FOR_APP, Constants.PendingTask.NONE)
            }
            else -> {
                // No-op for other states
            }
        }
    }
}