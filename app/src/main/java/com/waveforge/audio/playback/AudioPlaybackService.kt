package com.waveforge.audio.playback

import android.content.Context
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.waveforge.audio.WaveForgeApplication
import com.waveforge.audio.engine.pcm.HaasAudioProcessor
import com.waveforge.audio.engine.pcm.CrossfeedAudioProcessor
import com.waveforge.audio.engine.pcm.PcmDspBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AudioPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var pcmBackend: PcmDspBackend? = null

    override fun onCreate() {
        super.onCreate()
        val haasProcessor = HaasAudioProcessor()
        val crossfeedProcessor = CrossfeedAudioProcessor()
        pcmBackend = PcmDspBackend(haasProcessor, crossfeedProcessor)
        
        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(crossfeedProcessor, haasProcessor))
                    .build()
            }
        }
        
        exoPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .build()
            
        mediaSession = MediaSession.Builder(this, exoPlayer!!).build()
        
        val engine = (application as WaveForgeApplication).engine
        engine.attachPcmBackend(pcmBackend!!)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        pcmBackend?.release()
        val engine = (application as WaveForgeApplication).engine
        engine.detachSession(-1)
        
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}
