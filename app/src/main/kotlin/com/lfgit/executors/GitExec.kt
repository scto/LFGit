package com.lfgit.executors

import android.content.Context
import com.lfgit.R
import com.lfgit.database.model.Repo
import com.lfgit.utilites.Constants
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/** Git commands */
class GitExec(
    execCallback: ExecListener,
    private val mGitExecListener: GitExecListener,
    private val mContext: Context
) {
    private val mExecutor: BinaryExecutor = BinaryExecutor(execCallback)
    private val mGitPath = "git"
    private val mLfsPath = "git-lfs"

    /** Check if directory is a Git repository */
    fun isRepo(path: String) {
        mExecutor.run(mGitPath, path, "rev-parse", "--git-dir")
    }

    /** Set Git Profile credentials */
    fun configCreds(email: String, username: String) {
        mExecutor.run(mGitPath, ".", "config", "--global", "user.name", username)
        mExecutor.run(mGitPath, ".", "config", "--global", "user.email", email)
    }

    /** Set Git Profile email */
    fun setEmail(email: String) {
        mExecutor.run(mGitPath, ".", "config", "--global", "user.email", email)
    }

    /** Set Git Profile username */
    fun setUsername(username: String) {
        mExecutor.run(mGitPath, ".", "config", "--global", "user.name", username)
    }

    /** Set hooks path */
    fun configHooks() {
        mExecutor.run(mGitPath, ".", "config", "--global", "core.hooksPath", Constants.HOOKS_DIR)
    }

    fun init(localPath: String) {
        Constants.mkdirsIfNotExist(localPath)
        mExecutor.run(mGitPath, localPath, "init")
    }

    fun commit(localPath: String, message: String) {
        mExecutor.run(mGitPath, localPath, "commit", "-m", message)
    }

    fun clone(localPath: String, remoteURL: String) {
        Constants.mkdirsIfNotExist(localPath)
        mExecutor.run(mGitPath, localPath, "clone", remoteURL)
    }

    fun shallowClone(localPath: String, remoteURL: String, depth: String) {
        mExecutor.run(mGitPath, localPath, "clone", "--depth", depth, remoteURL)
    }

    fun status(localPath: String) {
        mExecutor.run(mGitPath, localPath, "status")
    }

    fun addAllToStage(localPath: String) {
        mExecutor.run(mGitPath, localPath, "add", ".")
    }

    fun listBranches(localPath: String) {
        mExecutor.run(mGitPath, localPath, "branch", "-a")
    }

    fun checkoutLocal(localPath: String, branch: String) {
        mExecutor.run(mGitPath, localPath, "checkout", branch)
    }

    fun checkoutRemote(localPath: String, branch: String) {
        mExecutor.run(mGitPath, localPath, "checkout", "--track", branch)
    }

    fun push(repo: Repo) {
        execWithCredentials(repo, "push")
    }

    fun pull(repo: Repo) {
        mExecutor.run(mGitPath, repo.localPath, "pull")
    }

    /** Execute a command with address http(s)://username:password@domain Handles URL encoding */
    private fun execWithCredentials(repo: Repo, vararg gitOperation: String) {
        val remoteURL = repo.remoteURL

        if (!remoteURL.matches("^https?://(?!.*//).*$".toRegex())) {
            mGitExecListener.onError(mContext.getString(R.string.http_only))
            return
        }

        val username = repo.username
        val password = repo.password
        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            mGitExecListener.onError(mContext.getString(R.string.no_creds))
            return
        }

        val encodedUsername: String
        val encodedPassword: String
        try {
            encodedUsername = URLEncoder.encode(username, "UTF-8")
            encodedPassword = URLEncoder.encode(password, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            mGitExecListener.onError(mContext.getString(R.string.encoding_creds_err))
            return
        }

        val parts = remoteURL.split("://")
        if (parts.size != 2) {
            mGitExecListener.onError(mContext.getString(R.string.http_only))
            return
        }

        val url = "${parts[0]}://$encodedUsername:$encodedPassword@${parts[1]}"
        mExecutor.run(mGitPath, repo.localPath, gitOperation[0], url)
    }

    fun getRemoteURL(repo: Repo) {
        mExecutor.run(mGitPath, repo.localPath, "config", "--get", "remote.origin.url")
    }

    fun addOriginRemote(repo: Repo, remoteURL: String) {
        mExecutor.run(mGitPath, repo.localPath, "remote", "add", "origin", remoteURL)
    }

    fun editOriginRemote(repo: Repo, remoteURL: String) {
        mExecutor.run(mGitPath, repo.localPath, "remote", "set-url", "origin", remoteURL)
    }

    fun log(localPath: String) {
        mExecutor.run(mGitPath, localPath, "log")
    }

    fun resetHard(localPath: String) {
        mExecutor.run(mGitPath, localPath, "reset", "--hard")
    }

    fun lfsPush(repo: Repo) {
        execWithCredentials(repo, "push", "--all")
    }

    fun lfsPull(repo: Repo) {
        mExecutor.run(mLfsPath, repo.localPath, "pull")
    }

    fun lfsInstall() {
        mExecutor.run(mLfsPath, ".", "install")
    }

    fun lfsTrackPattern(localPath: String, pattern: String) {
        mExecutor.run(mLfsPath, localPath, "track", pattern)
    }

    fun lfsUntrackPattern(localPath: String, pattern: String) {
        mExecutor.run(mLfsPath, localPath, "untrack", pattern)
    }

    fun lfsListPatterns(localPath: String) {
        mExecutor.run(mLfsPath, localPath, "track")
    }

    fun lfsListFiles(localPath: String) {
        mExecutor.run(mLfsPath, localPath, "ls-files")
    }

    fun lfsPrune(localPath: String) {
        mExecutor.run(mLfsPath, localPath, "prune")
    }

    fun lfsEnv(localPath: String) {
        mExecutor.run(mLfsPath, localPath, "env")
    }

    fun lfsStatus(localPath: String) {
        mExecutor.run(mLfsPath, localPath, "status")
    }
}