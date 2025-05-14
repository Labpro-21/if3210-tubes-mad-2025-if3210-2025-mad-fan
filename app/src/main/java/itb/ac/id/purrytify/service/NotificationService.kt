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
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

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
        const val ACTION_SEEK = "itb.ac.id.purrytify.ACTION_SEEK"
        const val ACTION_TOGGLE_FAVORITE = "itb.ac.id.purrytify.ACTION_TOGGLE_FAVORITE"
    }

    private var player: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var currentSong: Song? = null
    private val binder = NotificationBinder()
    private var progressUpdateExecutor: ScheduledExecutorService? = null

    interface PlayerCallback {
        fun onPlayPause()
        fun onNext()
        fun onPrevious()
        fun onStop()
        fun onToggleFavorite()
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

                playerCallback?.onPlayPause()

                Log.d("NotificationService", "Play Clicked (setupMediaSession)")
            }

            override fun onPause() {

                playerCallback?.onPlayPause()

                Log.d("NotificationService", "Pause Clicked (setupMediaSession)")
            }

            override fun onSkipToNext() {
                playerCallback?.onNext()

                Log.d("NotificationService", "Next Clicked (setupMediaSession)")
            }

            override fun onSkipToPrevious() {
                playerCallback?.onPrevious()

                Log.d("NotificationService", "Previous Clicked (setupMediaSession)")
            }

            override fun onSeekTo(pos: Long) {
                player?.seekTo(pos)
            }
            override fun onCustomAction(action: String, extras: Bundle?) {
                when (action) {
                    ACTION_TOGGLE_FAVORITE -> {
                        Log.d("NotificationService", "Favorite clicked from media session callback")
                        playerCallback?.onToggleFavorite()
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

        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)

        val albumArt = loadAlbumArt(song.imagePath)
        if (albumArt != null) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    fun updateNotification() {
        val song = currentSong ?: return
        val isPlaying = player?.isPlaying ?: false
        val currentPosition = player?.currentPosition ?: 0L
        val duration = song.duration

        val customFavoriteAction = PlaybackStateCompat.CustomAction.Builder(
            ACTION_TOGGLE_FAVORITE,
            if (song.isLiked) "Remove from Favorites" else "Add to Favorites",
            if (song.isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        ).build()

        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                currentPosition,
                1.0f
            )
            .addCustomAction(customFavoriteAction)

        mediaSession.setPlaybackState(playbackStateBuilder.build())

        Log.d("NotificationService", "Updating notification, isPlaying: ${player?.isPlaying}, position: $currentPosition/$duration")

        val playPauseAction = NotificationCompat.Action.Builder(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying) "Pause" else "Play",
            createPendingIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY)
        ).build()

        val favoriteAction = NotificationCompat.Action.Builder(
            if (song.isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
            if (song.isLiked) "Remove from Favorites" else "Add to Favorites",
            createPendingIntent(ACTION_TOGGLE_FAVORITE)
        ).build()

        val albumArt = loadAlbumArt(song.imagePath)
        Log.d("NotificationService", "ImagePath: ${song.imagePath}")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(albumArt)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(R.drawable.ic_previous, "Previous", createPendingIntent(ACTION_PREVIOUS))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_next, "Next", createPendingIntent(ACTION_NEXT))
            .addAction(favoriteAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2, 3)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createPendingIntent(ACTION_STOP))
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

        try {
            // URI
            if (imagePath.startsWith("content://")) {
                val uri = android.net.Uri.parse(imagePath)
                val inputStream = contentResolver.openInputStream(uri)
                return inputStream?.use { BitmapFactory.decodeStream(it) } ?: defaultBitmap
            }
            // file path biasa
            else {
                val file = File(imagePath)
                if (file.exists()) {
                    return BitmapFactory.decodeFile(imagePath) ?: defaultBitmap
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Error loading album art: ${e.message}")
        }

        return defaultBitmap
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
            putExtra("timestamp", System.currentTimeMillis())
        }

        Log.d("NotificationService", "Creating PendingIntent for action: $action with requestCode: ${getRequestCode(action)}")

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
            ACTION_SEEK -> 105
            ACTION_TOGGLE_FAVORITE -> 106
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
        Log.d("NotificationService", "onStartCommand received, action: ${intent?.action}")

        if (intent?.action != null) {
            handleAction(intent.action!!, intent)
        }
        return START_NOT_STICKY
    }

    private fun handleAction(action: String, intent: Intent?) {
        Log.d("NotificationService", "Handling action: $action")

        when (action) {
            ACTION_SEEK -> {
                intent?.getLongExtra("position", -1L)?.let { position: Long ->
                    if (position >= 0) {
                        player?.seekTo(position)
                        updateNotification()
                    }
                }
            }
            ACTION_PLAY, ACTION_PAUSE -> {
                player?.let {
                    if (it.isPlaying) {
                        Log.d("NotificationService", "Pause action triggered")
                        it.pause()
                    } else {
                        Log.d("NotificationService", "Play action triggered")
                        it.play()
                    }
                    updateNotification()
                }
                playerCallback?.onPlayPause()
                sendActionToActivity(action)
            }
            ACTION_NEXT -> {
                Log.d("NotificationService", "Next action triggered in handleAction")
                playerCallback?.onNext()
                sendActionToActivity(ACTION_NEXT)
            }
            ACTION_PREVIOUS -> {
                Log.d("NotificationService", "Previous action triggered in handleAction")
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
            ACTION_TOGGLE_FAVORITE -> {
                Log.d("NotificationService", "Toggle favorite action triggered")
                playerCallback?.onToggleFavorite()
                sendActionToActivity(ACTION_TOGGLE_FAVORITE)
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
        stopProgressUpdates()
        player = null
        mediaSession.release()
    }
}