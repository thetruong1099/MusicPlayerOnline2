package com.example.musicplayeronline2.data

import com.example.musicplayeronline2.model.Song

class CurrentData {

    companion object {
        val instance = CurrentData()
    }

    var currentSongs: MutableList<Song> = mutableListOf()
    var currentSongPos: Int = 0
    var currentShuffle: Int = 2
    var currentId: String? = null
    var statusOnOff: Boolean = false
    var size = currentSongs.size
    var checkInternet: Boolean = false
}