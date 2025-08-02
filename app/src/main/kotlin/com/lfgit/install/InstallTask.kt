package com.lfgit.install

import android.content.Context
import android.os.AsyncTask
import android.system.ErrnoException
import android.system.Os
import android.util.Pair
import com.lfgit.executors.ExecListener
import com.lfgit.executors.GitExec
import com.lfgit.executors.GitExecListener
import com.lfgit.utilites.Constants
import java.io.*
import java.util.zip.ZipInputStream

/** Install the bootstrap packages */
@Suppress("DEPRECATION")
class InstallTask(
    private val mListener: AsyncTaskListener,
    context: Context
) : AsyncTask<Boolean, Void, Boolean>(), ExecListener, GitExecListener {

    private var mInstalled = false
    private val mGitExec: GitExec = GitExec(this, this, context)

    override fun onExecStarted() {}

    override fun onExecFinished(result: String, errCode: Int) {
        if (!mInstalled) {
            mGitExec.lfsInstall()
            mInstalled = true
        }
    }

    override fun onError(errorMsg: String) {}

    private fun installFiles(): Boolean {
        val prefixFile = File(Constants.USR_DIR)
        if (prefixFile.isDirectory) {
            return true
        }

        val stagingPrefixFile = File(Constants.USR_STAGING_DIR)
        deleteFolder(stagingPrefixFile)

        val buffer = ByteArray(8096)
        val symlinks = ArrayList<Pair<String, String>>(50)
        val zipBytes = loadZipBytes()

        try {
            ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipInput ->
                var zipEntry: java.util.zip.ZipEntry?
                while (zipInput.nextEntry.also { zipEntry = it } != null) {
                    val currentEntry = zipEntry ?: continue
                    if (currentEntry.name == "SYMLINKS.txt") {
                        BufferedReader(InputStreamReader(zipInput)).forEachLine { line ->
                            val parts = line.split("→")
                            if (parts.size != 2) throw RuntimeException("Malformed symlink line: $line")
                            val oldPath = parts[1]
                            val newPath = "${Constants.USR_STAGING_DIR}/${parts[0]}"
                            symlinks.add(Pair(oldPath, newPath))
                            File(newPath).parentFile?.let { ensureDirectoryExists(it) }
                        }
                    } else {
                        val targetFile = File(Constants.USR_STAGING_DIR, currentEntry.name)
                        val isDirectory = currentEntry.isDirectory
                        ensureDirectoryExists(if (isDirectory) targetFile else targetFile.parentFile!!)

                        if (!isDirectory) {
                            FileOutputStream(targetFile).use { outStream ->
                                var readBytes: Int
                                while (zipInput.read(buffer).also { readBytes = it } != -1) {
                                    outStream.write(buffer, 0, readBytes)
                                }
                            }
                            if (currentEntry.name.startsWith("bin/") ||
                                currentEntry.name.startsWith("libexec") ||
                                currentEntry.name.startsWith("lib/apt/methods")
                            ) {
                                Os.chmod(targetFile.absolutePath, 0b111_000_000) // 0700
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Unable to read zipBytes", e)
        } catch (e: ErrnoException) {
            throw RuntimeException("Unable to read zipBytes", e)
        }

        if (symlinks.isEmpty()) throw RuntimeException("No SYMLINKS.txt encountered")
        symlinks.add(Pair("/system/bin/sh", "${Constants.USR_STAGING_DIR}/sh"))

        symlinks.forEach { symlink ->
            try {
                Os.symlink(symlink.first, symlink.second)
            } catch (e: ErrnoException) {
                throw RuntimeException(
                    "Unable to create symlink: ${symlink.first} → ${symlink.second}", e
                )
            }
        }

        if (!stagingPrefixFile.renameTo(prefixFile)) {
            throw RuntimeException("Unable to rename staging folder")
        }

        // Create a directory for Git Hooks
        val dir = File(Constants.HOOKS_DIR)
        if (!dir.exists()) {
            dir.mkdir()
        }
        // Install Git Hooks
        mGitExec.configHooks()
        return true
    }

    private fun ensureDirectoryExists(directory: File) {
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw RuntimeException("Unable to create directory: ${directory.absolutePath}")
        }
    }

    companion object {
        init {
            // Only load the shared library when necessary to save memory usage.
            System.loadLibrary("termux-bootstrap")
        }

        @JvmStatic
        external fun getZip(): ByteArray

        private fun loadZipBytes(): ByteArray = getZip()

        /** Delete a folder and all its content or throw. Don't follow symlinks. */
        fun deleteFolder(fileOrDirectory: File) {
            if (!fileOrDirectory.exists()) return

            try {
                if (fileOrDirectory.canonicalPath == fileOrDirectory.absolutePath && fileOrDirectory.isDirectory) {
                    fileOrDirectory.listFiles()?.forEach { deleteFolder(it) }
                }
            } catch (e: IOException) {
                throw RuntimeException("getCanonicalPath() IO exception", e)
            }

            if (!fileOrDirectory.delete()) {
                throw RuntimeException(
                    "Unable to delete ${if (fileOrDirectory.isDirectory) "directory" else "file"} ${fileOrDirectory.absolutePath}"
                )
            }
        }
    }

    override fun doInBackground(vararg params: Boolean?): Boolean {
        return installFiles()
    }

    override fun onPreExecute() {
        mListener.onTaskStarted()
    }

    override fun onPostExecute(result: Boolean) {
        mListener.onTaskFinished(result)
    }
}