package com.example.musicplayeronline2.ui.fragment

import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.utils.Status
import com.example.musicplayeronline2.viewModel.CurrentDataViewModel
import com.example.musicplayeronline2.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_info_song.*
import java.io.File


class InfoSongFragment : Fragment() {

    private val currentData = CurrentData.instance

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(requireActivity().application)
        )[MusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_song, container, false)
    }

    override fun onStart() {
        super.onStart()

        val currentDataViewModel =
            ViewModelProvider(requireActivity()).get(CurrentDataViewModel::class.java)

        currentDataViewModel.getCurrentSong().observe(viewLifecycleOwner) { song ->

            if (currentData.statusOnOff) {

                if (song.thumbnail == null) {
                    img_song.setImageResource(R.drawable.note_music)
                } else {
                    Glide.with(requireContext()).load(song.thumbnail).into(img_song)
                }

                musicViewModel.getInfoSong("audio", song.idSong).observe(this) {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                lav_loader.visibility = View.GONE
                                resource.data?.let { musicObject ->
                                    var string: String = ""
                                    for (i in musicObject.song.genres) {
                                        string = i.name + " " + string
                                    }
                                    tv_genres.text = string.trim()
                                }
                            }
                            Status.ERROR -> {
                                lav_loader.visibility = View.GONE
//                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG)
//                                    .show()
                                Log.d("aaaa", "refreshData: ${it.message}")
                            }
                            Status.LOADING -> {
                                lav_loader.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            } else {

                val thumbnail = android.media.ThumbnailUtils.createAudioThumbnail(
                    File(song.url),
                    Size(320, 320),
                    null
                )

                if (thumbnail == null) {
                    img_song.setImageResource(R.drawable.note_music)
                } else {
                    img_song.setImageBitmap(thumbnail)
                }

                if (song.genres == null) {
                    tv_genres.text = "Unknow Genres"
                } else tv_genres.text = song.genres
            }

            tv_name_song.text = song.name

            if (song.album == null) {
                tv_album_name.text = "Unknow Album"
            } else {
                tv_album_name.text = song.album
            }

            if (song.artist == null) {
                tv_artist.text = "Unknow Artist"
            } else {
                tv_artist.text = song.artist
            }


        }
    }
}