package com.example.musicplayeronline2.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.musicplayeronline2.model.database.SongDownloadTable
import com.example.musicplayeronline2.model.database.SongFavoriteTable
import retrofit2.http.DELETE

@Dao
interface MusicDAO {

    //Download Song
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMusicDownload(song: SongDownloadTable)

    @Query("SELECT * FROM song_download_table")
    fun getAllMusicDownload(): LiveData<MutableList<SongDownloadTable>>

    //Favorite Song

    @Insert
    suspend fun insertFavoriteSong(song: SongFavoriteTable)

    @Query("DELETE FROM song_favorite_table WHERE song_id_col = :songId")
    suspend fun deleteFavoriteSong(songId: String)

    @Query("SELECT favorite_col FROM song_favorite_table WHERE song_id_col = :songId")
    fun getStatusFavorite(songId: String): LiveData<Boolean>

    @Query("SELECT * FROM song_favorite_table")
    fun getFavoriteMusic(): LiveData<MutableList<SongFavoriteTable>>

}