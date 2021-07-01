package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class MusicDataSearch(
    @SerializedName("song")
    var songs: MutableList<SongSearch>
)