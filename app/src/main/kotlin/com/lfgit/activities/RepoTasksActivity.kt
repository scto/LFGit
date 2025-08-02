package com.lfgit.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider

import com.lfgit.R
import com.lfgit.adapters.RepoTasksAdapter
import com.lfgit.database.model.Repo
import com.lfgit.databinding.ActivityRepoTasksBinding
import com.lfgit.fragments.dialogs.*
import com.lfgit.view_models.RepoTasksViewModel

/**
 * An activity implementing Git tasks user interface.
 */
class RepoTasksActivity : BasicAbstractActivity() {
    private lateinit var mRightDrawer: RelativeLayout
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mCredsDialog: CredentialsDialog
    private lateinit var mRemoteDialog: RemoteDialog
    private lateinit var mCommitDialog: CommitDialog
    private lateinit var mCheckoutDialog: CheckoutDialog
    private lateinit var mPatternDialog: PatternDialog
    private lateinit var mRepoTasksViewModel: RepoTasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityRepoTasksBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_repo_tasks)
        mRepoTasksViewModel = ViewModelProvider(this).get(RepoTasksViewModel::class.java)
        binding.repoTasksViewModel = mRepoTasksViewModel
        binding.lifecycleOwner = this

        setupDrawer()
        setupDialogs()

        val repo = intent.getSerializableExtra(Repo.TAG) as? Repo
        mRepoTasksViewModel.setRepo(repo)
        repo?.let {
            title = it.displayName
        }

        mRepoTasksViewModel.noRepo.observe(this) { message ->
            showToastMsg(message)
            finish()
        }

        mRepoTasksViewModel.execResult.observe(this) { result ->
            mRepoTasksViewModel.processExecResult(result)
        }

        mRepoTasksViewModel.execPending.observe(this) { show ->
            toggleProgressDialog(show)
        }

        mRepoTasksViewModel.promptCredentials.observe(this) { show ->
            toggleDialog(show, mCredsDialog, "credsDialog")
        }

        mRepoTasksViewModel.promptAddRemote.observe(this) { show ->
            toggleDialog(show, mRemoteDialog, "remoteDialog")
        }

        mRepoTasksViewModel.promptCommit.observe(this) { show ->
            toggleDialog(show, mCommitDialog, "commitDialog")
        }

        mRepoTasksViewModel.promptCheckout.observe(this) { show ->
            toggleDialog(show, mCheckoutDialog, "checkoutDialog")
        }

        mRepoTasksViewModel.promptPattern.observe(this) { show ->
            toggleDialog(show, mPatternDialog, "patternDialog")
        }

        mRepoTasksViewModel.showToast.observe(this) { message ->
            showToastMsg(message)
        }
    }

    private fun hideDialog(tag: String) {
        val dialog = supportFragmentManager.findFragmentByTag(tag)
        if (dialog is DialogFragment) {
            dialog.dismiss()
        }
    }

    private fun showDialog(dialog: DialogFragment, tag: String) {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(tag)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(tag)
        dialog.show(ft, tag)
    }

    private fun toggleDialog(show: Boolean, dialog: DialogFragment, tag: String) {
        if (show) {
            showDialog(dialog, tag)
        } else {
            hideDialog(tag)
        }
    }

    private fun setupDialogs() {
        mCheckoutDialog = CheckoutDialog.newInstance()
        mCommitDialog = CommitDialog.newInstance()
        mCredsDialog = CredentialsDialog.newInstance()
        mRemoteDialog = RemoteDialog.newInstance()
        mPatternDialog = PatternDialog.newInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repo_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toggleDrawer) {
            if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                closeDrawer()
            } else {
                openDrawer()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawerLayout)
        mRightDrawer = findViewById(R.id.rightDrawer)
        val repoOperationList = findViewById<ListView>(R.id.repoOperationList)

        val drawerAdapter = RepoTasksAdapter(this, mRepoTasksViewModel)
        repoOperationList.adapter = drawerAdapter
        repoOperationList.onItemClickListener = drawerAdapter
    }

    fun closeDrawer() {
        mDrawerLayout.closeDrawer(mRightDrawer)
    }

    fun openDrawer() {
        mDrawerLayout.openDrawer(mRightDrawer)
    }
}