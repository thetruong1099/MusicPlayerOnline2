package com.example.musicplayeronline2.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private val currentData = CurrentData.instance

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private val connectionNetwork by lazy {
        ConnectionLiveData(application)
    }

    private val listSongAdapter: ListSongApiAdapter by lazy {
        ListSongApiAdapter(this, onItemClick)
    }

    private val onItemClick: (song: Song) -> Unit = { song ->
        if (currentData.checkInternet) {
            currentData.currentSongs.clear()
            currentData.currentSongs.add(song)
            currentData.statusOnOff = true
            addRelateSong(song.idSong)
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("music", song)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        btn_back_search.setOnClickListener { onBackPressed() }

        checkNetworkConnection()
    }

    private fun addRelateSong(idSong: String) {
        musicViewModel.getListRelatedSong("audio", idSong!!).observe(this) {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
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
                                        "http://api.mp3.zing.vn/api/streaming/audio/${i.idSong}/128"
                                    )
                                    musicList.add(song)
                                }
                                currentData.currentSongs.addAll(musicList)
                            }
                        }
                    }
                    Status.ERROR -> {
                        Log.d("aaaa", "refreshData: ${it.message}")
                    }
                    Status.LOADING -> {
                    }
                }

            }
        }
    }

    private fun checkNetworkConnection() {
        connectionNetwork.observe(this) { isConnected ->
            currentData.checkInternet = isConnected

            if (isConnected) {
                rv_music_search.visibility = View.VISIBLE
                tv_no_connected.visibility = View.GONE
                refreshData()
            } else {
                rv_music_search.visibility = View.GONE
                tv_no_connected.visibility = View.VISIBLE
            }
        }
    }

    private fun refreshData() {
        rv_music_search.apply {
            adapter = listSongAdapter
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                listSongAdapter.clearListSong()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    var keyword =
                        "${s.toString().lowercase()}"
                    musicViewModel.searchSong("artist,song,key,code", 500, keyword)
                        .observe(this@SearchActivity) {
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        lav_loading.visibility = View.GONE
                                        resource.data?.let { musicObject ->
                                            for (i in musicObject.data) {
                                                val musicList: MutableList<Song> = mutableListOf()
                                                for (s in i.songs) {
                                                    var song = Song(
                                                        s.idSong,
                                                        s.name,
                                                        s.artist,
                                                        null,
                                                        null,
                                                        "https://photo-resize-zmp3.zadn.vn/w320_r1x1_png/${s.thumb}",
                                                        "http://api.mp3.zing.vn/api/streaming/audio/${s.idSong}/128"
                                                    )
                                                    musicList.add(song)
                                                }
                                                listSongAdapter.setListSong(musicList)
                                            }
                                        }
                                    }
                                    Status.ERROR -> {
                                        lav_loading.visibility = View.GONE
                                        Toast.makeText(
                                            this@SearchActivity,
                                            it.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.d("aaaa", "refreshData: ${it.message}")
                                    }
                                    Status.LOADING -> {
                                        lav_loading.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                } else listSongAdapter.clearListSong()

            }

            override fun afterTextChanged(s: Editable?) {
                listSongAdapter.clearListSong()
            }
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}