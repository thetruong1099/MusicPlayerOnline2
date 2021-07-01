package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class MusicObject2(
    @SerializedName("data")
    var song: SongApi
)
