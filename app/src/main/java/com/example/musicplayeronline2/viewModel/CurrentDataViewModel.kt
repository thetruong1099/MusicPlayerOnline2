package com.example.musicplayeronline2.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayeronline2.model.Song
import java.lang.IllegalArgumentException

class CurrentDataViewModel : ViewModel() {
    private val currentSong = MutableLiveData<Song>()

    fun setCurrentSong(song: Song) {
        currentSong.value = song
    }

    fun getCurrentSong(): MutableLiveData<Song> {
        return currentSong
    }
}