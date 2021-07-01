package com.example.musicplayeronline2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicplayeronline2.database.dao.MusicDAO
import com.example.musicplayeronline2.model.database.SongDownloadTable
import com.example.musicplayeronline2.model.database.SongFavoriteTable

@Database(entities = [SongDownloadTable::class, SongFavoriteTable::class], version = 1)

abstract class MusicDatabase : RoomDatabase() {
    abstract fun getMusicDao(): MusicDAO

    companion object statics {
        @Volatile
        private var instance: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context, MusicDatabase::class.java, "MusicDataBase")
                        .build()
            }
            return instance!!
        }
    }
}