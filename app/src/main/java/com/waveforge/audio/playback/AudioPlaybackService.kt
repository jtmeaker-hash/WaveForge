package com.waveforge.audio.playback

import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.waveforge.audio.WaveForgeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AudioPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, exoPlayer!!).build()
        
        // Tie WaveForgeApplication.engine parameters to ExoPlayer
        val engine = WaveForgeApplication.engine
        scope.launch {
            combine(engine.pitch, engine.tempo, engine.masterBypassed) { pitch, tempo, bypassed ->
                Triple(pitch, tempo, bypassed)
            }.collect { (pitch, tempo, bypassed) ->
                if (bypassed) {
                    exoPlayer?.playbackParameters = PlaybackParameters(1.0f, 1.0f)
                } else {
                    exoPlayer?.playbackParameters = PlaybackParameters(tempo, pitch)
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}
