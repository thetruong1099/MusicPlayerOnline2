package com.example.musicplayeronline2.utils

object Extension {
    fun formatTime(time: Int): String {
        var totalOut = ""
        var totalNew = ""
        val seconds = (time % 60).toString()
        val minutes = (time / 60).toString()
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"

        if (seconds.length == 1) {
            return totalNew
        } else {
            return totalOut
        }
    }
}