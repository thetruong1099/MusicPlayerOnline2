package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class Genres(
    @SerializedName("name")
    var name: String
)