package com.lfgit.database

import androidx.lifecycle.LiveData
import androidx.room.*

import com.lfgit.database.model.Repo

@Dao
interface RepoDao {
    @get:Query("SELECT * from repo")
    val allRepos: LiveData<List<Repo>>

    // Fügt ein einzelnes Repository ein
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertRepo(repo: Repo)

    // Fügt ein Array von Repositories ein
    @Insert
    fun insertList(repos: List<Repo>)

    @Update
    fun updateRepos(repos: List<Repo>)

    @Query("UPDATE repo SET username = :username, password= :password WHERE id =:id")
    fun updateCredentials(username: String?, password: String?, id: Int)

    @Query("UPDATE repo SET remoteURL = :remoteURL WHERE id =:id")
    fun updateRemoteURL(remoteURL: String, id: Int)

    // Löscht das gesamte Repository
    @Query("DELETE FROM repo")
    fun deleteAll()

    // Zeigt Repositories nach Namen geordnet an
    @Query("SELECT * FROM repo ORDER BY localPath ASC")
    fun getAlphabetizedRepos(): LiveData<List<Repo>>

    @Query("DELETE FROM repo WHERE id = :repoId")
    fun deleteByRepoId(repoId: Int)

    @Query("DELETE FROM repo WHERE localPath = :localPath")
    fun deleteByLocalPath(localPath: String)
}