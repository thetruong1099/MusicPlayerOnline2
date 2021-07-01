package com.example.musicplayeronline2.ui.activity

import android.content.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.model.Song
import com.example.musicplayeronline2.model.database.SongDownloadTable
import com.example.musicplayeronline2.model.database.SongFavoriteTable
import com.example.musicplayeronline2.service.MusicService
import com.example.musicplayeronline2.service.MusicService.Companion.REPEAT_ALL
import com.example.musicplayeronline2.service.MusicService.Companion.REPEAT_ONE
import com.example.musicplayeronline2.service.MusicService.Companion.SHUFFLE_ALL
import com.example.musicplayeronline2.ui.adapter.ViewPagerAdapter
import com.example.musicplayeronline2.ui.fragment.AnimationMusicPlayerDiscFragment
import com.example.musicplayeronline2.ui.fragment.InfoSongFragment
import com.example.musicplayeronline2.utils.ConnectionLiveData
import com.example.musicplayeronline2.utils.Extension
import com.example.musicplayeronline2.utils.Status
import com.example.musicplayeronline2.viewModel.CurrentDataViewModel
import com.example.musicplayeronline2.viewModel.MusicViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_music_player.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import java.io.*

class MusicPlayerActivity : AppCompatActivity(), ServiceConnection {

    private val adapterViewPagers by lazy {
        ViewPagerAdapter(
            supportFragmentManager,
            lifecycle
        )
    }

    private val currentDataViewModel by lazy {
        ViewModelProvider(
            this,
        )[CurrentDataViewModel::class.java]
    }

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private lateinit var music: Song

    private val currentData = CurrentData.instance

    private lateinit var musicService: MusicService

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        job = Job()

        setViewPager()

        getDataFromIntent()

        handleSeekBar()

        initListenerBtn()

    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, MusicService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        applicationContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
        registerLocalBroadcastReceiver()
    }

    override fun onResume() {
        super.onResume()

        handleSeekBarCoroutine()
    }

    override fun onPause() {
        super.onPause()
        applicationContext.unbindService(this)
        job.cancel()
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadCastReceiver)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_stationary, R.anim.slide_out_down)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder = service as MusicService.MyBinder
        musicService = myBinder.service
        musicService.currentDataViewModel = currentDataViewModel
        musicService.musicViewModel = musicViewModel
        var position = currentData.currentSongs.indexOf(music)
        if (music.idSong != currentData.currentId) {
            musicService.playMusic(position)
        }
        musicService.initListener()
        refreshView()
        val serviceMsg = musicService.messenger
        try {
            val msg = Message.obtain(null, MusicService.MSG_REGISTER_CLIENT)
            msg.replyTo = messenger
            serviceMsg.send(msg)
        } catch (ignore: RemoteException) {
        }
    }

    private val messenger = Messenger(IncomingHandler())

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MusicService.MSG_COMPLETED) {
                refreshView()
            } else super.handleMessage(msg)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    private fun setViewPager() {

        var listFragment: MutableList<Fragment> = mutableListOf(
            InfoSongFragment(),
            AnimationMusicPlayerDiscFragment()
        )

        adapterViewPagers.setFragment(listFragment)

        view_pager_info_song.apply {
            adapter = adapterViewPagers
            setCurrentItem(listFragment.size / 2, false)
        }

        TabLayoutMediator(tab_dot, view_pager_info_song) { tab, position ->
        }.attach()

    }

    private fun getDataFromIntent() {
        music = intent.getSerializableExtra("music") as Song
    }

    private fun refreshView() {
        if (::musicService.isInitialized) {

            if (musicService.isPlaying) {
                btn_play.setImageResource(R.drawable.baseline_pause_24)
            } else {
                btn_play.setImageResource(R.drawable.baseline_play_arrow_24)
            }

            seekBar.max = musicService.duration / 1000
            val mCurrentPosition = musicService.currentPosition / 1000
            seekBar.progress = mCurrentPosition
            tv_current_time.text = Extension.formatTime(mCurrentPosition)
            tv_duration_time.text = Extension.formatTime(musicService.duration / 1000)
        }

        music = currentData.currentSongs[currentData.currentSongPos]

        tv_name_song.text = music.name
        tv_name_song.isSelected = true

        if (music.artist == null) {
            tv_artist.text = "Unknown Artist"
        } else {
            tv_artist.text = music.artist
        }

        tv_artist.isSelected = true

        setUiBtnShuffle()

        progress_bar_download.progress = 0

        initListenerFavorite()
    }

    private fun setUiBtnShuffle() {
        when (currentData.currentShuffle) {
            REPEAT_ONE -> {
                btn_shuffle.setImageResource(R.drawable.baseline_repeat_one_24)
            }
            REPEAT_ALL -> {
                btn_shuffle.setImageResource(R.drawable.baseline_repeat_24)
            }
            SHUFFLE_ALL -> {
                btn_shuffle.setImageResource(R.drawable.baseline_shuffle_24)
            }
        }
    }

    private fun handleSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (::musicService.isInitialized && fromUser) {
                    musicService.seekTo(progress * 1000)
                    seekBar!!.progress = progress
                    tv_current_time.text = Extension.formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    private fun handleSeekBarCoroutine() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (::musicService.isInitialized) {
                    val currentPosition = musicService.currentPosition / 1000
                    updateUi(currentPosition)
                }
                delay(1000)
            }
        }
    }

    suspend fun updateUi(process: Int) {
        withContext(Dispatchers.Main) {
            seekBar.progress = process
            tv_current_time.text = Extension.formatTime(process)
        }
    }

    private fun initListenerBtn() {

        btn_back.setOnClickListener {
            finish()
        }

        btn_play.setOnClickListener { playPauseBtnClicked() }
        btn_previous_song.setOnClickListener { prevBtnClicked() }
        btn_next_song.setOnClickListener { nextBtnClicked() }

        btn_shuffle.setOnClickListener {
            if (currentData.currentShuffle == 4) currentData.currentShuffle = 1
            else currentData.currentShuffle++
            setUiBtnShuffle()
        }

        btn_relate_song.setOnClickListener {
            var intent = Intent(this, RelateActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        btn_download.setOnClickListener {
            currentData.currentId?.let { idSong ->
                downloadFileFromApi(idSong)
            }
        }
    }

    private fun nextBtnClicked() {
        musicService.nextMusic()
        refreshView()

    }

    private fun prevBtnClicked() {
        musicService.previousMusic()
        refreshView()
    }

    private fun playPauseBtnClicked() {
        if (musicService.isPlaying) {
            btn_play.setImageResource(R.drawable.baseline_play_arrow_24)
        } else {
            btn_play.setImageResource(R.drawable.baseline_pause_24)
        }
        musicService.playPauseMusic()
    }

    private val localBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionName = intent?.getStringExtra("action_music")
            if (actionName == "action_music") {
                refreshView()
            }
        }
    }

    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localBroadCastReceiver, IntentFilter("send_data_to_activity"))
    }

    private fun downloadFileFromApi(idSong: String) {
        musicViewModel.getAllMusicDownload().observe(this) {
            var checkD = true
            for (i in it) {
                if (idSong == i.idSong) {
                    checkD = false
                    break
                }
            }

            if (checkD) {

                val url = "http://api.mp3.zing.vn/api/streaming/audio/${idSong}/128"

                musicViewModel.downloadFile(url).observe(this) {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                resource.data?.let { responseBody ->
                                    writeFile(responseBody, idSong)
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
            } else {
                Toast.makeText(
                    this@MusicPlayerActivity,
                    "File has been downloaded ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writeFile(body: ResponseBody, idSong: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                File("/data/data/$packageName/music").mkdir()
                val destinationFile = File("/data/data/$packageName/music/$idSong.mp3")

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {
                    val fileReader = ByteArray(4096)
                    var fileSizeDownloaded: Long = 0

                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(destinationFile)

                    while (true) {
                        val read = inputStream.read(fileReader)

                        if (read == -1) {
                            break
                        }

                        outputStream.write(fileReader, 0, read)

                        fileSizeDownloaded += read

                        progress_bar_download.max = 1
                        progress_bar_download.progress =
                            (fileSizeDownloaded / body.contentLength()).toInt()
                    }

                    outputStream.flush();

                    insertToDataBase(destinationFile, idSong)

//                    Toast.makeText(
//                        this@MusicPlayerActivity,
//                        "File download successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    Log.d("aaaa", "File saved successfully!")

                } catch (e: IOException) {
                    Log.d("aaaa", "Failed to save the file! ${e.message}");
                } finally {
                    inputStream?.let { inputStream.close() }

                    outputStream?.let { outputStream.close() }
                }

            } catch (e: IOException) {
                Log.d("aaaa", "Failed to save the file! ${e.message}")
            }
        }
    }

    private fun insertToDataBase(file: File, idSong: String) {
        val uri = Uri.fromFile(file)

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(this, uri)

        val name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        val genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
        val urlSong = "/data/data/$packageName/music/$idSong.mp3"

        //insert to database
        musicViewModel.insertMusicDownload(
            SongDownloadTable(
                idSong,
                name!!,
                artist!!,
                album,
                genre,
                urlSong
            )
        )
    }

    private fun initListenerFavorite() {
        val songId = currentData.currentId
        var statusFavorite = false
        songId?.let {
            musicViewModel.getStatusFavorite(songId).observe(this) {
                it?.let { status ->
                    if (status) {
                        statusFavorite = true
                        btn_favorite.setImageResource(R.drawable.baseline_favorite_24)
                    } else {
                        statusFavorite = false
                        btn_favorite.setImageResource(R.drawable.baseline_favorite_border_24)
                    }

                }
                btn_favorite.setOnClickListener {
                    if (statusFavorite) {
                        statusFavorite = false
                        btn_favorite.setImageResource(R.drawable.baseline_favorite_border_24)
                        musicViewModel.deleteFavoriteSong(songId)
                    } else {
                        statusFavorite = true
                        btn_favorite.setImageResource(R.drawable.baseline_favorite_24)

                        val song = currentData.currentSongs[currentData.currentSongPos]
                        val name = song.name
                        val artist = song.artist
                        val thumbnail = song.thumbnail
                        val code = song.code

                        musicViewModel.insertFavoriteSong(
                            SongFavoriteTable(
                                songId,
                                name,
                                artist,
                                thumbnail,
                                true,
                                code
                            )
                        )

                    }
                }
            }
        }
    }
}