package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class MusicObject3(
    @SerializedName("data")
    var data: MutableList<MusicDataSearch>
)