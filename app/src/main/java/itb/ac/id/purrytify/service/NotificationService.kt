package itb.ac.id.purrytify.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.MainActivity
import java.io.File

class NotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "purrytify_playback_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_PLAY = "itb.ac.id.purrytify.ACTION_PLAY"
        const val ACTION_PAUSE = "itb.ac.id.purrytify.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "itb.ac.id.purrytify.ACTION_PREVIOUS"
        const val ACTION_NEXT = "itb.ac.id.purrytify.ACTION_NEXT"
        const val ACTION_STOP = "itb.ac.id.purrytify.ACTION_STOP"

        const val ACTION_MUSIC_CONTROL = "itb.ac.id.purrytify.ACTION_MUSIC_CONTROL"
    }

    private var player: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var currentSong: Song? = null
    private val binder = NotificationBinder()

    interface PlayerCallback {
        fun onPlayPause()
        fun onNext()
        fun onPrevious()
        fun onStop()
    }

    private var playerCallback: PlayerCallback? = null

    fun setPlayerCallback(callback: PlayerCallback) {
        playerCallback = callback
    }

    inner class NotificationBinder : Binder() {
        fun getService(): NotificationService = this@NotificationService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "PurrytifyMediaSession")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Purrytify Playback"
            val descriptionText = "Controls for Purrytify music playback"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setPlayer(exoPlayer: ExoPlayer) {
        this.player = exoPlayer
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    stopForeground(true)
                }
            }
        })
    }

    fun updateCurrentSong(song: Song?) {
        currentSong = song
        updateNotification()
    }

    fun updateNotification() {
        val song = currentSong ?: return
        val isPlaying = player?.isPlaying ?: false

        Log.d("NotificationService", "Updating notification, isPlaying: ${player?.isPlaying}")



        val playPauseAction = NotificationCompat.Action.Builder(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying) "Pause" else "Play",
            createPendingIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY)
        ).build()

        // log
        Log.d("NotificationService", "PlayPauseAction: ${playPauseAction.title}")

        val albumArt = loadAlbumArt(song.imagePath)
//        log imagepath
        Log.d("NotificationService", "ImagePath: ${song.imagePath}")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(albumArt)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(R.drawable.ic_previous, "Previous", createPendingIntent(ACTION_PREVIOUS))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_next, "Next", createPendingIntent(ACTION_NEXT))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createContentIntent())
            .setSilent(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

//    TODO: ganti ke album art dari file
private fun loadAlbumArt(imagePath: String): Bitmap {
    val file = File(imagePath)
    return if (file.exists()) {
        BitmapFactory.decodeFile(imagePath)
    } else {
        BitmapFactory.decodeResource(resources, R.drawable.logo)
    }
}

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
            putExtra("unique_key", System.currentTimeMillis())
        }
        return PendingIntent.getBroadcast(
            this,
            getRequestCode(action),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getRequestCode(action: String): Int {
        return when (action) {
            ACTION_PLAY -> 100
            ACTION_PAUSE -> 101
            ACTION_PREVIOUS -> 102
            ACTION_NEXT -> 103
            ACTION_STOP -> 104
            else -> 0
        }
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null) {
            handleAction(intent.action!!)
        }
        return START_NOT_STICKY
    }

    private fun handleAction(action: String) {
        when (action) {
            // Broadcast intent ke MainActivity
            ACTION_PLAY, ACTION_PAUSE -> {
                player?.let {
                    if (it.isPlaying) {
                        it.pause()
                    } else {
                        it.play()
                    }
                    updateNotification()
                }
                playerCallback?.onPlayPause()
                sendActionToActivity(action)
            }
            ACTION_NEXT -> {
                playerCallback?.onNext()
                sendActionToActivity(ACTION_NEXT)
            }
            ACTION_PREVIOUS -> {
                playerCallback?.onPrevious()
                sendActionToActivity(ACTION_PREVIOUS)
            }
            ACTION_STOP -> {
                player?.stop()
                playerCallback?.onStop()
                sendActionToActivity(ACTION_STOP)
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun sendActionToActivity(action: String) {
        val intent = Intent(ACTION_MUSIC_CONTROL).apply {
            putExtra("command", action)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        player = null
        mediaSession.release()
    }
}