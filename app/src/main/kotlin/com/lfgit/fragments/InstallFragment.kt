package com.lfgit.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.lfgit.R
import com.lfgit.activities.RepoListActivity
import com.lfgit.install.AsyncTaskListener
import com.lfgit.install.InstallTask

/**
 * Install packages. Handle activity lifecycle.
 *
 * source:
 * https://androidresearch.wordpress.com/2013/05/10/dealing-with-asynctask-and-screen-orientation/
 */
class InstallFragment : Fragment(), AsyncTaskListener {
    private var mProgressDialog: ProgressDialog? = null
    /* private*/ var isTaskRunning = false
    /* private*/ var isFirstRun = true
    private var mActivity: RepoListActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // If we are returning here from a screen orientation
        // and the AsyncTask is still working, re-create and display the
        // progress dialog.
        if (isTaskRunning) {
            showProgressDialog()
        }
        if (isFirstRun) {
            val installer = InstallTask(this, mActivity!!)
            installer.execute(true)
            isFirstRun = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_repo_list, container, false)
    }

    override fun onTaskStarted() {
        isTaskRunning = true
        showProgressDialog()
    }

    override fun onTaskFinished(installed: Boolean) {
        mProgressDialog?.dismiss()
        isTaskRunning = false
        mActivity?.onPackagesInstalled(installed)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as RepoListActivity
    }

    override fun onDetach() {
        // All dialogs should be closed before leaving the activity in order to avoid
        // the: Activity has leaked window com.android.internal.policy... exception
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
        mActivity = null
        super.onDetach()
    }

    private fun showProgressDialog() {
        val title = mActivity?.resources?.getString(R.string.install_progress_title)
        val msg = mActivity?.resources?.getString(R.string.install_progress_msg)
        mProgressDialog = ProgressDialog.show(activity, title, msg)
    }
}