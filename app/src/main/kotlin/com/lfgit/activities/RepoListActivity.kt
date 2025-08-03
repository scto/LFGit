package com.lfgit.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.commit

import com.lfgit.BuildConfig
import com.lfgit.R
import com.lfgit.adapters.RepoListAdapter
import com.lfgit.databinding.ActivityRepoListBinding
import com.lfgit.fragments.InstallFragment
import com.lfgit.utilites.Constants
import com.lfgit.utilites.UriHelper
import com.lfgit.view_models.RepoListViewModel

/**
 * Eine Activity, die die Liste der Repositories und die Erstinstallation implementiert.
 */
class RepoListActivity : BasicAbstractActivity() {

    // Verwende View Binding, um auf Views zuzugreifen
    private lateinit var binding: ActivityRepoListBinding
    
    // Verwende den KTX Property-Delegaten, um den ViewModel zu initialisieren
    private val repoListViewModel: RepoListViewModel by viewModels()
    
    private lateinit var repoListAdapter: RepoListAdapter
    private val installPref = InstallPreference()

    companion object {
        private const val ADD_REPO_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialisiere View Binding
        binding = ActivityRepoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installIfNeeded(repoListViewModel)

        binding.lifecycleOwner = this
        binding.repoListViewModel = repoListViewModel

        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        repoListAdapter = RepoListAdapter(this, repoListViewModel)
        binding.repoList.apply {
            adapter = repoListAdapter
            onItemClickListener = repoListAdapter
            onItemLongClickListener = repoListAdapter
        }

        binding.repoListLayout.setOnRefreshListener {
            repoListAdapter.refreshRepos()
            binding.repoListLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        repoListViewModel.showToast.observe(this, ::showToastMsg)

        repoListViewModel.allRepos.observe(this) { repoList ->
            repoListAdapter.setRepos(repoList)
            repoListViewModel.setRepos(repoList)
        }

        repoListViewModel.execResult.observe(this) { result ->
            repoListViewModel.processExecResult(result)
        }
    }

    /** Installiert Pakete bei Bedarf oder fragt nur nach Berechtigungen */
    private fun installIfNeeded(viewModel: RepoListViewModel) {
        if (!viewModel.isInstalling()) {
            if (installPref.isFirstRun()) {
                viewModel.setInstalling(true)
                runInstallFragment()
            } else {
                checkAndRequestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun runInstallFragment() {
        // Verwende die Fragment KTX-Erweiterung für eine saubere Transaktion
        supportFragmentManager.commit {
            add(R.id.repoListLayout, InstallFragment())
        }
    }

    fun onPackagesInstalled(installed: Boolean) {
        repoListViewModel.setInstalling(false)
        if (installed) {
            installPref.updateInstallPreference()
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
        // Verwende 'when' anstelle von 'switch'
        when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.menu_init_repo -> {
                startActivity(Intent(this, AddRepoActivity::class.java))
            }
            R.id.menu_add_repo -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, ADD_REPO_REQUEST_CODE)
            }
            R.id.menu_refresh -> {
                repoListAdapter.refreshRepos()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_REPO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Sicheres Entpacken der URI mit 'let'
            data?.data?.let { uri ->
                val path = UriHelper.getStoragePathFromURI(this, uri)
                when {
                    path == null -> {
                        showToastMsg(application.getString(R.string.storage_not_supported))
                    }
                    Constants.isWritablePath(path) -> {
                        repoListViewModel.addLocalRepo(path)
                    }
                    else -> {
                        showToastMsg(application.getString(R.string.no_write_dir))
                    }
                }
            }
        }
    }

    /** Speichert die Installationspräferenz mit dem Versionscode */
    inner class InstallPreference {
        private val prefsName = "mInstallPref"
        private val prefVersionCodeKey = "version_code"
        private val currentVersionCode = BuildConfig.VERSION_CODE
        private val doesntExist = -1

        private val sharedPreferences by lazy {
            getSharedPreferences(prefsName, MODE_PRIVATE)
        }

        fun isFirstRun(): Boolean {
            val savedVersionCode = sharedPreferences.getInt(prefVersionCodeKey, doesntExist)
            return currentVersionCode != savedVersionCode
        }

        fun updateInstallPreference() {
            // Verwende die KTX 'edit' Erweiterung
            sharedPreferences.edit().putInt(prefVersionCodeKey, currentVersionCode).apply()
        }
    }
}