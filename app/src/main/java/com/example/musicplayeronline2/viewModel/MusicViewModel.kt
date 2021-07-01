package com.example.musicplayeronline2.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.example.musicplayeronline2.model.database.SongDownloadTable
import com.example.musicplayeronline2.model.database.SongFavoriteTable
import com.example.musicplayeronline2.repository.MusicRepository
import com.example.musicplayeronline2.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MusicViewModel(application: Application) : ViewModel() {

    private val musicRepository = MusicRepository(application)

    //api

    fun getListMusicRank(
        songId: Int,
        videoId: Int,
        albumId: Int,
        chart: String,
        time: Int
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        try {
            emit(
                Resource.success(
                    musicRepository.getListMusicRank(
                        songId,
                        videoId,
                        albumId,
                        chart,
                        time
                    )
                )
            )
        } catch (ex: Exception) {
            emit(Resource.error(null, ex.message ?: "ERROR !!"))
        }
    }

    fun getListRelatedSong(type: String, id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        try {
            emit(Resource.success(musicRepository.getListRelatedSong(type, id)))
        } catch (ex: Exception) {
            emit(Resource.error(null, ex.message ?: "ERROR !!"))
        }
    }

    fun getInfoSong(type: String, id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        try {
            emit(Resource.success(musicRepository.getInfoSong(type, id)))
        } catch (ex: Exception) {
            emit(Resource.error(null, ex.message ?: "ERROR !!"))
        }
    }

    fun downloadFile(url: String) = liveData(Dispatchers.IO) {

        emit(Resource.loading(null))
        try {
            kotlinx.coroutines.delay(300)
            emit(Resource.success(musicRepository.downloadFile(url)))
        } catch (ex: Exception) {
            emit(Resource.error(null, ex.message ?: "ERROR !!"))
        }

    }

    fun searchSong(type: String, num: Int, query: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        try {
            emit(Resource.success(musicRepository.searchSong(type, num, query)))
        } catch (ex: Exception) {
            emit(Resource.error(null, ex.message ?: "ERROR !!"))
        }
    }

    fun getListMusicUSUK(op: String, start: Int, length: Int, id: String) =
        liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            try {
                emit(Resource.success(musicRepository.getListMusicUSUK(op, start, length, id)))
            } catch (ex: Exception) {
                emit(Resource.error(null, ex.message ?: "ERROR !!"))
            }
        }

    fun getInfoAlbum(type: String, key: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        try {
            emit(Resource.success(musicRepository.getInfoAlbum(type, key)))
        } catch (ex: Exception) {
            emit(Resource.error(null, ex.message ?: "ERROR !!"))
        }
    }

    //database

    //download

    fun insertMusicDownload(song: SongDownloadTable) =
        viewModelScope.launch(Dispatchers.IO) { musicRepository.insertMusicDownload(song) }

    fun getAllMusicDownload(): LiveData<MutableList<SongDownloadTable>> =
        musicRepository.getAllMusicDownload()

    //favorite

    fun insertFavoriteSong(song: SongFavoriteTable) =
        viewModelScope.launch(Dispatchers.IO) { musicRepository.insertFavoriteSong(song) }

    fun deleteFavoriteSong(songId: String) =
        viewModelScope.launch(Dispatchers.IO) { musicRepository.deleteFavoriteSong(songId) }

    fun getStatusFavorite(songId: String): LiveData<Boolean> =
        musicRepository.getStatusFavorite(songId)

    fun getFavoriteMusic(): LiveData<MutableList<SongFavoriteTable>> =
        musicRepository.getFavoriteMusic()

    //factory

    class MusicViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(application) as T
            }

            throw IllegalArgumentException("unable construct viewModel")
        }

    }
}