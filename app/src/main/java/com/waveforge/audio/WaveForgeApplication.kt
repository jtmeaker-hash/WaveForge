package com.waveforge.audio

import android.app.Application
import android.content.Intent
import com.waveforge.audio.engine.AudioEngine
import com.waveforge.audio.engine.WaveForgeAudioService

class WaveForgeApplication : Application() {
    companion object {
        lateinit var engine: AudioEngine
            private set
    }

    override fun onCreate() {
        super.onCreate()
        engine = AudioEngine(this)
        
        // Start foreground service to keep engine alive
        val intent = Intent(this, WaveForgeAudioService::class.java)
        try {
            startForegroundService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
