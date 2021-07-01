package com.example.musicplayeronline2.ui.activity

import android.content.*
import android.os.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.service.MusicService
import com.example.musicplayeronline2.utils.ConnectionLiveData
import com.example.musicplayeronline2.viewModel.CurrentDataViewModel
import com.example.musicplayeronline2.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    private val currentData = CurrentData.instance

    private lateinit var musicService: MusicService

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private val currentDataViewModel by lazy {
        ViewModelProvider(
            this,
        )[CurrentDataViewModel::class.java]
    }

    private var lavProcess: Float = 0.0F

    private val localBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionName = intent?.getStringExtra("action_music")
            if (actionName == "action_music") {
                refreshView()
            }
        }
    }

    private lateinit var job: Job

    private val connectionNetwork by lazy {
        ConnectionLiveData(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationControl()

        job = Job()

        checkNetworkConnection()
    }

    override fun onStart() {
        super.onStart()

        if (currentData.currentId != null) {

            layout_my_song.visibility = View.VISIBLE

            val intent = Intent(this, MusicService::class.java)
            ContextCompat.startForegroundService(this, intent)

            applicationContext.bindService(intent, this, Context.BIND_AUTO_CREATE)

            initListenerBtn()

            registerLocalBroadcastReceiver()

            startMediaPlayerActivity()

            refreshView()

            handleProcessBar()
        } else {
            layout_my_song.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadCastReceiver)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder = service as MusicService.MyBinder
        musicService = myBinder.service

        musicService.currentDataViewModel = currentDataViewModel
        musicService.musicViewModel = musicViewModel

        refreshView()

        musicService.initListener()
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

    private fun navigationControl() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHost.navController
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun refreshView() {
        currentData.currentId?.let {
            val music = currentData.currentSongs[currentData.currentSongPos]
            if (::musicService.isInitialized) {
                if (musicService.isPlaying) {
                    btn_play_main.setImageResource(R.drawable.baseline_pause_24)
                    lav_music_disc_main.setMinProgress(lavProcess)
                    lav_music_disc_main.playAnimation()
                    lav_music_disc_main.setMinProgress(0.0F)
                } else {
                    btn_play_main.setImageResource(R.drawable.baseline_play_arrow_24)
                    lav_music_disc_main.pauseAnimation()
                    lavProcess = lav_music_disc_main.progress
                }

                progress_bar.max = musicService.duration / 1000
                progress_bar.progress = musicService.currentPosition / 1000
            }

            tv_name_song_main.text = music.name
            if (music.artist == null) {
                tv_name_singer_main.text = "Unknown Artist"
            } else {
                tv_name_singer_main.text = "${music.artist}"
            }
        }
    }

    private fun initListenerBtn() {
        if (::musicService.isInitialized) {
            btn_play_main.setOnClickListener {
                if (musicService.isPlaying) {
                    btn_play_main.setImageResource(R.drawable.baseline_play_arrow_24)
                    lav_music_disc_main.pauseAnimation()
                    lavProcess = lav_music_disc_main.progress
                    job.cancel()
                } else {
                    btn_play_main.setImageResource(R.drawable.baseline_pause_24)
                    lav_music_disc_main.setMinProgress(lavProcess)
                    lav_music_disc_main.playAnimation()
                    lav_music_disc_main.setMinProgress(0.0F)
                    handleProcessBar()
                }
                musicService.playPauseMusic()
            }

            btn_next_main.setOnClickListener {
                musicService.nextMusic()
                refreshView()
            }
        }
    }

    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localBroadCastReceiver, IntentFilter("send_data_to_activity"))
    }

    private fun startMediaPlayerActivity() {
        layout_my_song.setOnClickListener {
            val music = currentData.currentSongs[currentData.currentSongPos]
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("music", music)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
        }
    }

    private fun handleProcessBar() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (::musicService.isInitialized) {
                    updateUIProcessBar(musicService.currentPosition, musicService.duration)
                }
                delay(1000)
            }
        }
    }

    suspend fun updateUIProcessBar(position: Int, positionMax: Int) {
        withContext(Dispatchers.Main) {
            progress_bar.max = positionMax / 1000
            progress_bar.progress = position / 1000
        }
    }

    private fun checkNetworkConnection() {
        connectionNetwork.observe(this) { isConnected ->
            currentData.checkInternet = isConnected
        }
    }

}