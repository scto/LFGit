package com.lfgit.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.lfgit.R
import com.lfgit.executors.ExecListener
import com.lfgit.executors.GitExec
import com.lfgit.executors.GitExecListener
import com.lfgit.utilites.Logger.LogDebugMsg

/** Set preference settings */
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, ExecListener, GitExecListener {

    private var mGitExec: GitExec? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val prefValue = sharedPreferences.getString(key, "")
        LogDebugMsg(prefValue)
        when (key) {
            getString(R.string.git_username_key) -> mGitExec?.setUsername(prefValue)
            getString(R.string.git_email_key) -> mGitExec?.setEmail(prefValue)
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mGitExec = GitExec(this, this, context)
    }

    override fun onDetach() {
        mGitExec = null
        super.onDetach()
    }

    override fun onExecStarted() {}

    override fun onExecFinished(result: String, errCode: Int) {}

    override fun onError(errorMsg: String) {}
}