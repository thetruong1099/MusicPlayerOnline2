package com.example.musicplayeronline2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.model.Song
import com.example.musicplayeronline2.ui.activity.MusicPlayerActivity
import com.example.musicplayeronline2.ui.activity.SearchActivity
import com.example.musicplayeronline2.ui.adapter.ListSongApiAdapter
import com.example.musicplayeronline2.utils.ConnectionLiveData
import com.example.musicplayeronline2.utils.Status
import com.example.musicplayeronline2.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_rank.*


class RankFragment : Fragment() {

    private val currentData = CurrentData.instance

    private var tempListSong: MutableList<Song> = mutableListOf()

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(requireActivity().application)
        )[MusicViewModel::class.java]
    }

    private val connectionNetwork by lazy {
        ConnectionLiveData(requireActivity().application)
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
        return inflater.inflate(R.layout.fragment_rank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_music_rank.apply {
            adapter = listSongAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        swipe_layout.setOnRefreshListener {
            refreshData()
        }

        startSearchActivity()

        checkNetworkConnection()
    }

    private fun refreshData() {
        musicViewModel.getListMusicRank(0, 0, 0, "song", -1).observe(viewLifecycleOwner) {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        swipe_layout.isRefreshing = false
                        resource.data?.let { musicObject ->
                            musicObject.data.songs?.let { listSong ->

                                val musicList: MutableList<Song> = mutableListOf()

                                for (i in listSong) {
                                    var song = Song(
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

                                listSongAdapter.setListSong(musicList)
                                tempListSong = musicList
                            }

                        }
                    }
                    Status.ERROR -> {
                        swipe_layout.isRefreshing = false
                        Log.d("aaaa", "refreshData: ${it.message}")
                    }
                    Status.LOADING -> {
                        swipe_layout.isRefreshing = true
                    }
                }
            }
        }
    }

    private fun startSearchActivity() {
        btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }

    private fun checkNetworkConnection() {
        connectionNetwork.observe(viewLifecycleOwner) { isConnected ->
            currentData.checkInternet = isConnected

            if (isConnected) {
                tv_no_conneted.visibility = View.GONE
                swipe_layout.visibility = View.VISIBLE
                refreshData()
            } else {
                tv_no_conneted.visibility = View.VISIBLE
                swipe_layout.visibility = View.GONE
            }

        }
    }
}