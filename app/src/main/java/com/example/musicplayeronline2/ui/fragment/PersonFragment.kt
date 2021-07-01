package com.example.musicplayeronline2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.model.Song
import com.example.musicplayeronline2.ui.activity.MusicPlayerActivity
import com.example.musicplayeronline2.ui.adapter.ListSongApiAdapter
import com.example.musicplayeronline2.utils.ConnectionLiveData
import com.example.musicplayeronline2.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_person.*


class PersonFragment : Fragment() {

    private val currentData = CurrentData.instance

    private var tempListSong: MutableList<Song> = mutableListOf()

    private val connectionNetwork by lazy {
        ConnectionLiveData(requireActivity().application)
    }

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(requireActivity().application)
        )[MusicViewModel::class.java]
    }

    private val listSongAdapter by lazy {
        ListSongApiAdapter(requireContext(), onItemClick)
    }

    private val onItemClick: (song: Song) -> Unit = { song ->
        if (currentData.checkInternet) {
            currentData.currentSongs = tempListSong
            currentData.statusOnOff = true
            var intent = Intent(requireContext(), MusicPlayerActivity::class.java)
            intent.putExtra("music", song)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false)
    }

    override fun onStart() {
        super.onStart()

        rv_favorite_song.apply {
            adapter = listSongAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        musicViewModel.getFavoriteMusic().observe(this) { musicList ->
            val songList: MutableList<Song> = mutableListOf()
            for (i in musicList) {
                val song = Song(
                    i.idSong,
                    i.nameSong,
                    i.artist,
                    null,
                    null,
                    i.thumbnail,
                    "http://api.mp3.zing.vn/api/streaming/audio/${i.idSong}/128",
                    i.code
                )
                songList.add(song)
            }
            listSongAdapter.setListSong(songList)
            tempListSong = songList
        }

        checkNetworkConnection()
    }

    private fun checkNetworkConnection() {
        connectionNetwork.observe(this) { isConnected ->
            currentData.checkInternet = isConnected
        }
    }
}