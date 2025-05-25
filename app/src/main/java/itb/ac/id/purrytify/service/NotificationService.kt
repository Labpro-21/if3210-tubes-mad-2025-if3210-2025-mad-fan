package itb.ac.id.purrytify.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.MainActivity
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class NotificationService : Service() {
    companion object {
        private const val CHANNEL_ID = "purrytify_playback_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_TOGGLE_FAVORITE = "itb.ac.id.purrytify.ACTION_TOGGLE_FAVORITE"
        const val ACTION_DISMISS_STOP = "itb.ac.id.purrytify.ACTION_DISMISS_STOP"
        const val ACTION_MUSIC_CONTROL = "itb.ac.id.purrytify.ACTION_MUSIC_CONTROL"
    }

    private var player: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var currentSong: Song? = null
    private val binder = NotificationBinder()
    private var progressUpdateExecutor: ScheduledExecutorService? = null

    private val imageCache = ConcurrentHashMap<String, Bitmap>()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    interface PlayerCallback {
        fun onPlayPause()
        fun onNext()
        fun onPrevious()
        fun onStop()
        fun onToggleFavorite()
        fun onDismissStop()
//        fun onSeek(position: Long)
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
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                Log.d("NotificationService", "Play clicked from MediaSession")
                playerCallback?.onPlayPause()
                sendActionToActivity("PLAY")
            }

            override fun onPause() {
                Log.d("NotificationService", "Pause clicked from MediaSession")
                playerCallback?.onPlayPause()
                sendActionToActivity("PAUSE")
            }

            override fun onSkipToNext() {
                Log.d("NotificationService", "Next clicked from MediaSession")
                playerCallback?.onNext()
                sendActionToActivity("NEXT")
            }

            override fun onSkipToPrevious() {
                Log.d("NotificationService", "Previous clicked from MediaSession")
                playerCallback?.onPrevious()
                sendActionToActivity("PREVIOUS")
            }

            override fun onSeekTo(pos: Long) {
                Log.d("NotificationService", "Seek to position: $pos")
                player?.seekTo(pos)
                sendActionToActivity("SEEK")
            }

            override fun onStop() {
                Log.d("NotificationService", "Stop clicked from MediaSession")
                playerCallback?.onStop()
                sendActionToActivity("STOP")
                stopForeground(true)
                stopSelf()
            }

            override fun onCustomAction(action: String, extras: Bundle?) {
                when (action) {
                    ACTION_TOGGLE_FAVORITE -> {
                        Log.d("NotificationService", "Favorite clicked from MediaSession")
                        playerCallback?.onToggleFavorite()
                        sendActionToActivity("TOGGLE_FAVORITE")
                    }
                    ACTION_DISMISS_STOP -> {
                        Log.d("NotificationService", "Dismiss/Stop clicked from MediaSession")
                        playerCallback?.onDismissStop()
                        sendActionToActivity("DISMISS_STOP")
                        stopForeground(true)
                        stopSelf()
                    }
                }
            }
        })
        mediaSession.isActive = true
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
        startProgressUpdates()
    }

    private fun setupPlayerListener() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    stopForeground(true)
                    stopProgressUpdates()
                }
            }
        })
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateExecutor = Executors.newSingleThreadScheduledExecutor()
        progressUpdateExecutor?.scheduleAtFixedRate({
            updateNotification()
        }, 0, 1000, TimeUnit.MILLISECONDS)
    }

    private fun stopProgressUpdates() {
        progressUpdateExecutor?.shutdown()
        progressUpdateExecutor = null
    }

    fun updateCurrentSong(song: Song?) {
        currentSong = song
        updateMediaSessionMetadata()
        updateNotification()
    }

    private fun updateMediaSessionMetadata() {
        val song = currentSong ?: return
//        log song filepath
        Log.d("NotificationService", "Song filePath: ${song.filePath}")
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)

//        log imagePath
        Log.d("NotificationService", "ImagePath: ${song.imagePath}")
        val albumArt = loadAlbumArt(song.imagePath)
        if (albumArt != null) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    private fun isOnlineSong(filePath: String): Boolean {
        return filePath.startsWith("http://") || filePath.startsWith("https://")
    }

    fun updateNotification() {
        val song = currentSong ?: return
        val isPlaying = player?.isPlaying ?: false
        val currentPosition = player?.currentPosition ?: 0L
        val duration = song.duration

        val isLocalSong = !isOnlineSong(song.filePath)

        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                currentPosition,
                1.0f
            )

        if (isLocalSong) {
            val customFavoriteAction = PlaybackStateCompat.CustomAction.Builder(
                ACTION_TOGGLE_FAVORITE,
                if (song.isLiked) "Remove from Favorites" else "Add to Favorites",
                if (song.isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            ).build()

            playbackStateBuilder.addCustomAction(customFavoriteAction)
        }

        val customDismissStopAction = PlaybackStateCompat.CustomAction.Builder(
            ACTION_DISMISS_STOP,
            "Dismiss and Stop",
            R.drawable.ic_close
        ).build()

        playbackStateBuilder.addCustomAction(customDismissStopAction)

        mediaSession.setPlaybackState(playbackStateBuilder.build())

        Log.d("NotificationService", "Updating notification, isPlaying: $isPlaying, position: $currentPosition/$duration")

        val albumArt = loadAlbumArt(song.imagePath)
        Log.d("NotificationService", "ImagePath: ${song.imagePath}")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(albumArt)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2) // Show prev, play/pause, next
                    .setShowCancelButton(true)
            )
            .setProgress(duration.toInt(), currentPosition.toInt(), false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createContentIntent())
            .setSilent(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun loadAlbumArt(imagePath: String): Bitmap {
        val defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        if (imagePath.isEmpty()) {
            return defaultBitmap
        }

        // cache first
        imageCache[imagePath]?.let { return it }

        try {
            // online
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                loadImageFromUrlAsync(imagePath)
                return defaultBitmap
            }
            // URI
            else if (imagePath.startsWith("content://")) {
                val uri = android.net.Uri.parse(imagePath)
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) } ?: defaultBitmap
                imageCache[imagePath] = bitmap
                return bitmap
            }
            // file path biasa
            else {
                val file = File(imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imagePath) ?: defaultBitmap
                    imageCache[imagePath] = bitmap
                    return bitmap
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Error loading album art: ${e.message}")
        }

        return defaultBitmap
    }

    private fun loadImageFromUrlAsync(url: String) {
        serviceScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val connection = java.net.URL(url).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000
                    connection.doInput = true
                    connection.connect()

                    val inputStream = connection.getInputStream()
                    inputStream.use { BitmapFactory.decodeStream(it) }
                }

                if (bitmap != null) {
                    imageCache[url] = bitmap

                    withContext(Dispatchers.Main) {
                        updateNotification()
                        updateMediaSessionMetadata()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Error loading image from URL: ${e.message}")
            }
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
        Log.d("NotificationService", "onStartCommand received")
        return START_NOT_STICKY
    }

    private fun sendActionToActivity(action: String) {
        val intent = Intent(ACTION_MUSIC_CONTROL).apply {
            putExtra("command", action)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdates()
        serviceScope.cancel()
        imageCache.clear()
        player = null
        mediaSession.release()
    }
}