package com.example.musicplayeronline2.model.api

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("name")
    var name: String?,
)
