package com.example.musicplayeronline2.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.musicplayeronline2.api.ApiConfig
import com.example.musicplayeronline2.database.MusicDatabase
import com.example.musicplayeronline2.database.dao.MusicDAO
import com.example.musicplayeronline2.model.database.SongDownloadTable
import com.example.musicplayeronline2.model.database.SongFavoriteTable

class MusicRepository(app: Application) {

    //api

    suspend fun getListMusicRank(
        songId: Int,
        videoId: Int,
        albumId: Int,
        chart: String,
        time: Int
    ) = ApiConfig.apiService.getListMusicRank(songId, videoId, albumId, chart, time)

    suspend fun getListRelatedSong(type: String, id: String) =
        ApiConfig.apiService.getListRelatedSong(type, id)

    suspend fun getInfoSong(type: String, id: String) = ApiConfig.apiService.getInfoSong(type, id)

    suspend fun downloadFile(url: String) = ApiConfig.apiService2.downloadFileByUrl(url)

    suspend fun searchSong(type: String, num: Int, query: String) =
        ApiConfig.apiService3.searchSong(type, num, query)

    suspend fun getListMusicUSUK(
        op: String,
        start: Int,
        length: Int,
        id: String
    ) = ApiConfig.apiService.getlistMusicUSUK(op, start, length, id)

    suspend fun getInfoAlbum(type: String, key: String) =
        ApiConfig.apiService.getInfoAlbum(type, key)
    //database

    private val musicDAO: MusicDAO

    init {
        val musicDatabase: MusicDatabase = MusicDatabase.getInstance(app)
        musicDAO = musicDatabase.getMusicDao()
    }

    suspend fun insertMusicDownload(song: SongDownloadTable) = musicDAO.insertMusicDownload(song)

    fun getAllMusicDownload(): LiveData<MutableList<SongDownloadTable>> =
        musicDAO.getAllMusicDownload()

    suspend fun insertFavoriteSong(song: SongFavoriteTable) = musicDAO.insertFavoriteSong(song)

    suspend fun deleteFavoriteSong(songId: String) = musicDAO.deleteFavoriteSong(songId)

    fun getStatusFavorite(songId: String): LiveData<Boolean> = musicDAO.getStatusFavorite(songId)

    fun getFavoriteMusic(): LiveData<MutableList<SongFavoriteTable>> = musicDAO.getFavoriteMusic()
}