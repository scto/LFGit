package com.lfgit.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lfgit.BuildConfig
import com.lfgit.R
import com.lfgit.adapters.RepoListAdapter
import com.lfgit.databinding.ActivityRepoListBinding
import com.lfgit.fragments.InstallFragment
import com.lfgit.utilites.Constants
import com.lfgit.utilites.UriHelper
import com.lfgit.view_models.RepoListViewModel

/**
 * An activity implementing list of repositories and initial installation.
 */
class RepoListActivity : BasicAbstractActivity() {
    private lateinit var mRepoListViewModel: RepoListViewModel
    private lateinit var mRepoListAdapter: RepoListAdapter
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private val mInstallPref by lazy { InstallPreference() }
    private val mManager by lazy { supportFragmentManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRepoListViewModel = ViewModelProvider(this)[RepoListViewModel::class.java]
        installIfNeeded(mRepoListViewModel)

        val mBinding: ActivityRepoListBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_repo_list)
        mBinding.lifecycleOwner = this
        mBinding.repoListViewModel = mRepoListViewModel

        mRepoListAdapter = RepoListAdapter(this, mRepoListViewModel)
        mBinding.repoList.adapter = mRepoListAdapter
        mBinding.repoList.onItemClickListener = mRepoListAdapter
        mBinding.repoList.onItemLongClickListener = mRepoListAdapter

        mRepoListViewModel.showToast.observe(this, this::showToastMsg)

        mRepoListViewModel.allRepos.observe(this) { repoList ->
            mRepoListAdapter.setRepos(repoList)
            mRepoListViewModel.setRepos(repoList)
        }

        mRepoListViewModel.execResult.observe(this) { result ->
            mRepoListViewModel.processExecResult(result)
        }

        pullToRefresh = findViewById(R.id.repoListLayout)
        pullToRefresh.setOnRefreshListener {
            mRepoListAdapter.refreshRepos()
            pullToRefresh.isRefreshing = false
        }
    }

    /** Install packages if needed, or just ask for permissions */
    private fun installIfNeeded(viewModel: RepoListViewModel) {
        if (!viewModel.isInstalling) {
            if (mInstallPref.isFirstRun) {
                viewModel.isInstalling = true
                runInstallFragment()
            } else {
                checkAndRequestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun runInstallFragment() {
        val transaction = mManager.beginTransaction()
        val fragment = InstallFragment()
        transaction.add(R.id.repoListLayout, fragment)
        transaction.commit()
    }

    fun onPackagesInstalled(installed: Boolean) {
        mRepoListViewModel.isInstalling = false
        if (installed) {
            mInstallPref.updateInstallPreference()
            checkAndRequestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            showToastMsg(getString(R.string.install_success))
        } else {
            showToastMsg(getString(R.string.install_failed))
            finishAffinity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repo_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.menu_settings -> {
                intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
            }
            R.id.menu_init_repo -> {
                intent = Intent(this, AddRepoActivity::class.java)
                this.startActivity(intent)
            }
            R.id.menu_add_repo -> {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, ADD_REPO_REQUEST_CODE)
            }
            R.id.menu_refresh -> mRepoListAdapter.refreshRepos()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == ADD_REPO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            intent?.data?.let { uri ->
                val path = UriHelper.getStoragePathFromURI(this, uri)
                when {
                    path == null -> showToastMsg(application.getString(R.string.storage_not_supported))
                    Constants.isWritablePath(path) -> mRepoListViewModel.addLocalRepo(path)
                    else -> showToastMsg(application.getString(R.string.no_write_dir))
                }
            }
        }
    }

    /** Save install preference with version code */
    inner class InstallPreference {
        private val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        private val currentVersionCode = BuildConfig.VERSION_CODE

        val isFirstRun: Boolean
            get() {
                val savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST)
                return currentVersionCode != savedVersionCode
            }

        fun updateInstallPreference() {
            prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply()
        }

        private companion object {
            const val PREFS_NAME = "mInstallPref"
            const val PREF_VERSION_CODE_KEY = "version_code"
            const val DOESNT_EXIST = -1
        }
    }

    private companion object {
        const val ADD_REPO_REQUEST_CODE = 1
    }
}