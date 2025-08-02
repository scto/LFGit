package com.lfgit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lfgit.database.model.Repo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Repo::class], version = 1, exportSchema = false)
abstract class RepoDatabase : RoomDatabase() {
    abstract fun repoDao(): RepoDao

    companion object {
        @Volatile
        private var INSTANCE: RepoDatabase? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor: ExecutorService =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        @Synchronized
        fun getInstance(context: Context): RepoDatabase {
            // Singleton-Datenbank
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RepoDatabase::class.java, "DB_REPO"
                )
                    // Löscht und baut neu, anstatt zu migrieren,
                    // wenn kein Migrationsobjekt vorhanden ist.
                    // TODO: Migration bei Bedarf
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}