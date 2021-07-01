package com.example.musicplayeronline2.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayeronline2.R
import com.example.musicplayeronline2.application.ApplicationClass
import com.example.musicplayeronline2.application.ApplicationClass.Companion.CHANNEL_ID
import com.example.musicplayeronline2.data.CurrentData
import com.example.musicplayeronline2.model.Song
import com.example.musicplayeronline2.receiver.MusicReceiver
import com.example.musicplayeronline2.utils.ConnectionLiveData
import com.example.musicplayeronline2.utils.NetworkHelper
import com.example.musicplayeronline2.utils.Status
import com.example.musicplayeronline2.viewModel.CurrentDataViewModel
import com.example.musicplayeronline2.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.activity_relate.*
import java.lang.Exception
import java.util.*
import kotlin.random.Random

class MusicService : Service() {

    companion object {
        const val REPEAT_ONE = 1
        const val REPEAT_ALL = 2
        const val SHUFFLE_ALL = 3
        const val MSG_REGISTER_CLIENT = 0
        const val MSG_COMPLETED = 1
    }

    private var mBinder: IBinder = MyBinder()

    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private val currentData = CurrentData.instance

    private val currentSongsList: MutableList<Song> get() = currentData.currentSongs
    private val currentPos: Int get() = currentData.currentSongPos
    private val currentShuffle: Int get() = currentData.currentShuffle

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var mediaSessionCompat: MediaSessionCompat

    val isPlaying get() = mediaPlayer.isPlaying
    val duration get() = mediaPlayer.duration
    val currentPosition get() = mediaPlayer.currentPosition

    lateinit var currentDataViewModel: CurrentDataViewModel
    lateinit var musicViewModel: MusicViewModel

    inner class MyBinder : Binder() {
        val service: MusicService get() = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(baseContext, "My Audio")
    }

//    @SuppressLint("MissingSuperCall")
//    override fun onBind(intent: Intent?): IBinder? {
//        return mBinder
//    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val actionName = intent?.getStringExtra("ActionName")

        if (actionName != null) {
            when (actionName) {
                "playPause" -> {
                    playPauseMusic()

                }
                "next" -> {
                    nextMusic()

                }
                "previous" -> {
                    previousMusic()

                }
                "clear" -> {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    fun playMusic(position: Int) {

        if (mediaPlayer.isPlaying) {
        mediaPlayer.stop()
        mediaPlayer.release()
        }

        createMediaPlayer(position)
        mediaPlayer.start()
        startNotification()
    }

    private fun createMediaPlayer(positionInner: Int) {
        if (positionInner < currentSongsList.size) {
            currentData.currentSongPos = positionInner
            currentData.currentId = currentData.currentSongs[positionInner].idSong
            currentDataViewModel.setCurrentSong(currentData.currentSongs[positionInner])
            val uri = currentData.currentSongs[positionInner].url
            mediaPlayer = MediaPlayer()
            mediaPlayer.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(uri)
//                try {
                    prepare()
//                } catch (e: Exception) {
//                    Log.d("aaaa", "createMediaPlayer: ${e.message}")
//                }

            }
        }
    }

    fun playPauseMusic() {

        checkNetworkConnected()

        if (isPlaying) {
            mediaPlayer.pause()
            updateNotification()
        } else {
            mediaPlayer.start()
            updateNotification()
        }
        sendActionToActivity()
    }

    fun nextMusic() {

        checkNetworkConnected()

        if ((currentData.statusOnOff && currentData.checkInternet)
            || (!currentData.statusOnOff && currentData.checkInternet)
            || (!currentData.statusOnOff && !currentData.checkInternet)
        ) {
            mediaPlayer.stop()
            mediaPlayer.release()

            when (currentShuffle) {
                REPEAT_ALL -> currentData.currentSongPos =
                    ((currentData.currentSongPos + 1) % currentSongsList.size)
                SHUFFLE_ALL -> currentData.currentSongPos =
                    Random.nextInt(0, currentSongsList.size - 1)
            }
            createMediaPlayer(currentPos)
            initListener()
            mediaPlayer.start()
            updateNotification()
            sendActionToActivity()
        }
    }

    fun previousMusic() {

        checkNetworkConnected()

        if ((currentData.statusOnOff && currentData.checkInternet)
            || (!currentData.statusOnOff && currentData.checkInternet)
            || (!currentData.statusOnOff && !currentData.checkInternet)
        ) {
            mediaPlayer.stop()
            mediaPlayer.release()
            when (currentShuffle) {
                REPEAT_ALL -> currentData.currentSongPos =
                    if (currentData.currentSongPos == 0) currentSongsList.size - 1
                    else currentData.currentSongPos - 1
                SHUFFLE_ALL -> currentData.currentSongPos =
                    Random.nextInt(0, currentSongsList.size - 1)
            }

            createMediaPlayer(currentPos)
            initListener()
            mediaPlayer.start()
            updateNotification()
            sendActionToActivity()
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun initListener() {
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.stop()
            mediaPlayer.release()

            checkNetworkConnected()

            if ((currentData.statusOnOff && currentData.checkInternet)
                || (!currentData.statusOnOff && currentData.checkInternet)
                || (!currentData.statusOnOff && !currentData.checkInternet)
            ) {
                when (currentData.currentShuffle) {
                    REPEAT_ALL -> currentData.currentSongPos =
                        ((currentData.currentSongPos + 1) % currentSongsList.size)
                    SHUFFLE_ALL -> currentData.currentSongPos =
                        Random.nextInt(0, currentSongsList.size - 1)
                }
            }

            createMediaPlayer(currentPos)
            initListener()
            mediaPlayer.start()
            updateNotification()
            sentMsg(MSG_COMPLETED)
        }
    }

    private fun sentMsg(msg: Int) {
        for (client in clients) {
            try {
                client.send(Message.obtain(null, msg))
            } catch (e: RemoteException) {
                clients.remove(client)
            }
        }
    }

    val messenger = Messenger(IncomingHandler())
    val clients = ArrayList<Messenger>()

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_REGISTER_CLIENT) {
                clients.add(msg.replyTo)
            } else super.handleMessage(msg)
        }
    }

    private fun showNotification(): Notification {
        val prevIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_PREVIOUS)
        val prevPending =
            PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_PLAY)
        val pausePending =
            PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_NEXT)
        val nextPending =
            PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val clearIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_CLEAR)
        val clearPending =
            PendingIntent.getBroadcast(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.note_music)

        val artistName = if (currentSongsList[currentPos].artist == null) {
            "Unknown Artist"
        } else {
            currentSongsList[currentPos].artist
        }

        notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .setLargeIcon(bitmap)
                .setContentTitle(currentSongsList[currentPos].name)
                .setContentText(artistName)
                .setOnlyAlertOnce(true)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.sessionToken)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isPlaying) {
            notificationBuilder
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
                .addAction(R.drawable.baseline_pause_24, "Pause", pausePending)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
                .addAction(R.drawable.baseline_clear_24, "Clear", clearPending)
        } else {
            notificationBuilder
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
                .addAction(R.drawable.baseline_play_arrow_24, "Pause", pausePending)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
                .addAction(R.drawable.baseline_clear_24, "Clear", clearPending)
        }

        return notificationBuilder.build()
    }

    private fun startNotification() {
        startForeground(2, showNotification())
    }

    private fun updateNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, showNotification())
    }

    private fun sendActionToActivity() {
        val intent = Intent("send_data_to_activity")
        intent.putExtra("action_music", "action_music")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun checkNetworkConnected() {
        currentData.checkInternet = NetworkHelper.isNetworkConnected(this)
//        Log.d("aaaa", "onCreate b: ${NetworkHelper.isNetworkConnected(this)}")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mediaPlayer?.let {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
//        Log.d("aaaa", "onDestroy: ")
    }
}