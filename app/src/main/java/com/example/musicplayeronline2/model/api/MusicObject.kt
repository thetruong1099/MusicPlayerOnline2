package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class MusicObject(
    @SerializedName("data")
    var data: MusicData,
)
