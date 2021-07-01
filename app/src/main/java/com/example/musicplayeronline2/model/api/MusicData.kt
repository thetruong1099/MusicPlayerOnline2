package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class MusicData(

    @SerializedName("song")
    var songs: MutableList<SongApi>,

    @SerializedName("items")
    var items: MutableList<SongApi>
)
