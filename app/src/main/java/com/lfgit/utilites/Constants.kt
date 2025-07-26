package com.lfgit.utilites

import android.os.Environment
import java.io.File

object Constants {
    const val PKG = "com.lfgit/"
    const val APP_DIR = "/data/data/$PKG"
    const val FILES_DIR = APP_DIR + "files"
    const val USR_DIR = "$FILES_DIR/usr"
    const val USR_STAGING_DIR = "$FILES_DIR/usr-staging"
    const val LIB_DIR = "$USR_DIR/lib"
    const val BIN_DIR = "$USR_DIR/bin"
    const val REPOS_DIR = APP_DIR + "/repos"
    const val GIT_CORE_DIR = "$FILES_DIR/libexec/git-core"
    const val HOOKS_DIR = "$FILES_DIR/hooks"
    var EXT_STORAGE: String = Environment.getExternalStorageDirectory().toString() + "/"

    /** Pending Git Task */
    enum class PendingTask {
        CLONE,
        SHALLOW_CLONE,
        INIT,
        COMMIT,
        ADD,
        PUSH,
        PULL,
        STATUS,
        LOG,
        RESET_HARD,
        ADD_REMOTE,
        SET_REMOTE,
        LIST_BRANCHES,
        CHECKOUT_LOCAL,
        CHECKOUT_REMOTE,
        LFS_TRACK,
        LFS_UNTRACK,
        LFS_LIST_PATTERNS,
        LFS_LIST_FILES,
        LFS_PRUNE,
        LFS_STATUS,
        LFS_ENV,
        CONFIG,
        NONE,
    }

    /** Inner state of a task */
    enum class InnerState {
        IS_REPO,
        FOR_APP,
        FOR_USER,
        GET_REMOTE_GIT,
        ADD_ORIGIN_REMOTE,
        SET_ORIGIN_REMOTE,
    }

    /** Check if path is writable. */
    fun isWritablePath(path: String): Boolean {
        val f = File(path)
        return f.canWrite()
    }

    /** Make directories if they don't exist yet. Returns path as a File. */
    fun mkdirsIfNotExist(path: String): File {
        val f = File(path)
        if (!f.exists()) {
            f.mkdirs()
        }
        return f
    }

    /** Returns the Git directory name ( = the project name) */
    fun getGitDir(path: String): String {
        var p = path
        if (p.endsWith("/")) {
            p = p.substring(0, p.length - 1)
        }
        var lastPathSegment = p.substring(p.lastIndexOf("/") + 1)

        val index = lastPathSegment.lastIndexOf(".git")
        if (index > 0) {
            lastPathSegment = lastPathSegment.substring(0, index)
        }
        return lastPathSegment
    }
}