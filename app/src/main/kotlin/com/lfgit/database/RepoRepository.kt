package com.lfgit.database

import android.app.Application
import androidx.lifecycle.LiveData

import com.lfgit.database.RepoDatabase
import com.lfgit.database.model.Repo

import java.io.File

class RepoRepository(application: Application) {
    private val mRepoDao: RepoDao
    val allRepos: LiveData<List<Repo>>

    init {
        val db = RepoDatabase.getInstance(application)
        mRepoDao = db.repoDao()
        allRepos = mRepoDao.allRepos
    }

    fun insertRepo(repo: Repo) {
        RepoDatabase.databaseWriteExecutor.execute { mRepoDao.insertRepo(repo) }
    }

    fun insertList(repos: List<Repo>) {
        RepoDatabase.databaseWriteExecutor.execute { mRepoDao.insertList(repos) }
    }

    fun deleteByID(repoId: Int) {
        RepoDatabase.databaseWriteExecutor.execute { mRepoDao.deleteByRepoId(repoId) }
    }

    fun updateCredentials(repo: Repo) {
        val username = repo.username
        val password = repo.password
        val id = repo.id
        RepoDatabase.databaseWriteExecutor.execute {
            mRepoDao.updateCredentials(username, password, id)
        }
    }

    fun updateRemoteURL(repo: Repo) {
        val remoteURL = repo.remoteURL
        val id = repo.id
        RepoDatabase.databaseWriteExecutor.execute { mRepoDao.updateRemoteURL(remoteURL, id) }
    }

    fun repoDirExists(repo: Repo): Boolean {
        val path = repo.localPath
        val file = File(path)
        return file.exists()
    }
}