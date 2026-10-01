package com.waveforge.audio

import android.app.Application
import android.content.Intent
import com.waveforge.audio.engine.AudioEngine
import com.waveforge.audio.engine.WaveForgeAudioService

class WaveForgeApplication : Application() {
    lateinit var engine: AudioEngine
        private set

    override fun onCreate() {
        super.onCreate()
        engine = AudioEngine(this)
        
        try {
            startForegroundService(Intent(this, WaveForgeAudioService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
