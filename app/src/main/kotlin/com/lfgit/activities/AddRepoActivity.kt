package com.lfgit.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

import com.lfgit.R
import com.lfgit.databinding.ActivityInitRepoBinding
import com.lfgit.utilites.UriHelper
import com.lfgit.view_models.AddRepoViewModel

/** Init/Clone activity */
class AddRepoActivity : BasicAbstractActivity() {
    private lateinit var mBinding: ActivityInitRepoBinding
    private lateinit var mAddRepoViewModel: AddRepoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_init_repo)
        mAddRepoViewModel = ViewModelProvider(this).get(AddRepoViewModel::class.java)
        mBinding.addRepoViewModel = mAddRepoViewModel
        mBinding.lifecycleOwner = this

        mAddRepoViewModel.execResult.observe(this) { result ->
            mAddRepoViewModel.processExecResult(result)
        }

        mAddRepoViewModel.allRepos.observe(this) { repoList ->
            mAddRepoViewModel.setRepos(repoList)
        }

        mAddRepoViewModel.cloneResult.observe(this) { cloneResult ->
            showToastMsg(cloneResult)
            finish()
        }

        mAddRepoViewModel.initResult.observe(this) { initResult ->
            showToastMsg(initResult)
            finish()
        }

        mAddRepoViewModel.execPending.observe(this) { isPending ->
            if (isPending) showProgressDialog()
            else hideProgressDialog()
        }

        mAddRepoViewModel.showToast.observe(this) { message ->
            showToastMsg(message)
        }
    }

    fun cloneBrowseButtonHandler(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, CLONE_BROWSE_REQUEST_CODE)
    }

    fun initBrowseButtonHandler(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, INIT_BROWSE_REQUEST_CODE)
    }

    fun setRepoPath(intent: Intent, requestCode: Int): String? {
        val uri = intent.data
        val path = uri?.let { UriHelper.getStoragePathFromURI(this, it) }

        if (requestCode == INIT_BROWSE_REQUEST_CODE) {
            mBinding.addRepoViewModel?.initRepoPath = path
        } else if (requestCode == CLONE_BROWSE_REQUEST_CODE) {
            mBinding.addRepoViewModel?.cloneRepoPath = path
        }
        return path
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            intent?.let {
                val path = setRepoPath(it, requestCode)
                if (requestCode == INIT_BROWSE_REQUEST_CODE) {
                    mBinding.initPathEditText.setText(path)
                } else if (requestCode == CLONE_BROWSE_REQUEST_CODE) {
                    mBinding.cloneLocalPathEditText.setText(path)
                }
            }
        }
    }

    companion object {
        private const val INIT_BROWSE_REQUEST_CODE = 1
        private const val CLONE_BROWSE_REQUEST_CODE = 2
    }
}