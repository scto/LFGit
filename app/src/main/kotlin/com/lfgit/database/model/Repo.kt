package com.lfgit.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.lfgit.utilites.Constants

import java.io.Serializable

@Entity(tableName = "repo")
data class Repo(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var localPath: String,
    var remoteURL: String = "",
    var username: String? = null,
    var password: String? = null
) : Serializable {

    val displayName: String
        get() = Constants.getGitDir(localPath)

    companion object {
        const val TAG = "Repo"
        private const val serialVersionUID = -556977004352408504L
    }
}