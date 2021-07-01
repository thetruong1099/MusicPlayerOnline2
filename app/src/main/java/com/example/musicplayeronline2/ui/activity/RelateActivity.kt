package com.example.musicplayeronline2.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.model.Song
import com.example.musicplayeronline2.ui.adapter.ListSongApiAdapter
import com.example.musicplayeronline2.utils.ConnectionLiveData
import com.example.musicplayeronline2.utils.Status
import com.example.musicplayeronline2.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.activity_relate.*

class RelateActivity : AppCompatActivity() {

    private val currentData = CurrentData.instance

    private var tempListSong: MutableList<Song> = mutableListOf()

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private val connectionNetwork by lazy {
        ConnectionLiveData(application)
    }

    private val listSongAdapter by lazy {
        ListSongApiAdapter(this, onItemClick)
    }

    private val onItemClick: (song: Song) -> Unit = { song ->

        if (currentData.checkInternet) {
            currentData.currentSongs = tempListSong
            currentData.statusOnOff = true
            var intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("music", song)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relate)

        btn_clear.setOnClickListener {
            onBackPressed()
        }

        checkNetworkConnection()
    }

    private fun checkNetworkConnection() {
        connectionNetwork.observe(this) { isConnected ->
            currentData.checkInternet = isConnected

            if (isConnected) {

                tv_no_connected.visibility = View.GONE
                rv_relate_song.visibility = View.VISIBLE

                refreshData()

            } else {
                tv_no_connected.visibility = View.VISIBLE
                rv_relate_song.visibility = View.GONE
            }

        }
    }

    private fun refreshData() {
        rv_relate_song.apply {
            adapter = listSongAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        currentData.currentId?.let { id ->
            musicViewModel.getListRelatedSong("audio", id).observe(this) {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            lav_loading.visibility = View.GONE
                            rv_relate_song.visibility = View.VISIBLE
                            resource.data?.let { musicObject ->
                                musicObject.data.items?.let { listSong ->
                                    val musicList: MutableList<Song> = mutableListOf()
                                    for (i in listSong) {
                                        val song = Song(
                                            i.idSong,
                                            i.name,
                                            i.artists_names,
                                            i.album?.name,
                                            null,
                                            i.thumbnail,
                                            "http://api.mp3.zing.vn/api/streaming/audio/${i.idSong}/128",
                                            i.code
                                        )
                                        musicList.add(song)
                                    }

                                    listSongAdapter.setListSong(musicList)
                                    tempListSong = musicList

                                }
                            }
                        }
                        Status.ERROR -> {
                            lav_loading.visibility = View.GONE
                            rv_relate_song.visibility = View.VISIBLE
                            Log.d("aaaa", "refreshData: ${it.message}")
                        }
                        Status.LOADING -> {
                            lav_loading.visibility = View.VISIBLE
                            rv_relate_song.visibility = View.GONE
                        }
                    }

                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}