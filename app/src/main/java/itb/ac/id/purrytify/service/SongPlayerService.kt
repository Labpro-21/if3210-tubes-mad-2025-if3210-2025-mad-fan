//package itb.ac.id.purrytify.service
//
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.session.MediaSession
//import androidx.media3.session.MediaSessionService
//
//class SongPlayerService : MediaSessionService() {
//    private lateinit var player: ExoPlayer
//    private lateinit var mediaSession: MediaSession
//
//    override fun onCreate() {
//        super.onCreate()
//
//        player = ExoPlayer.Builder(this).build()
//
//        mediaSession = MediaSession.Builder(this, player)
//            .setId("media_session")
//            .build()
//
//    }
//
//    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
//        return mediaSession
//    }
//}