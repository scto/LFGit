package com.lfgit.executors

import com.lfgit.utilites.Constants
import com.lfgit.utilites.Logger.LogDebugMsg
import java.io.File
import java.io.IOException

/** Execute a compiled program. */
internal class BinaryExecutor(private val mCallback: ExecListener) {

    private val mExeDir: String = Constants.BIN_DIR

    fun run(binary: String, destDir: String, vararg strings: String) {
        val exeBin = "$mExeDir/$binary"
        val args = mutableListOf(exeBin)
        args.addAll(strings)

        LogDebugMsg("exe: ${args.toTypedArray().contentToString()}")

        val pb = ProcessBuilder(args).apply {
            redirectErrorStream(true) // redirect error stream to input stream
            directory(File(destDir))
            environment().apply {
                put("LD_LIBRARY_PATH", "${Constants.LIB_DIR}:${Constants.BIN_DIR}")
                put("PATH", Constants.BIN_DIR)
                put("HOME", Constants.FILES_DIR)
                put("XDG_CONFIG_HOME", Constants.FILES_DIR)
            }
        }

        val process: Process
        try {
            process = pb.start()
        } catch (e: IOException) {
            throw RuntimeException("Unable to start process", e)
        }

        Thread {
            val outBuffer = StringBuilder()
            val result: String
            val errCode: Int
            mCallback.onExecStarted()

            try {
                process.inputStream.bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        outBuffer.append(line).append(System.lineSeparator())
                    }
                }
            } catch (e: IOException) {
                // ignore
            }

            try {
                errCode = process.waitFor()
                result = outBuffer.toString()
                mCallback.onExecFinished(result, errCode)
            } catch (e: InterruptedException) {
                // ignore
            }
        }.start()
    }
}