package com.waveforge.audio.setup

import android.content.Context
import android.media.audiofx.AudioEffect
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.Spatializer
import android.os.Build
import android.media.AudioManager

data class CapabilityReport(
    val apiLevel: Int,
    val hasDynamicsProcessing: Boolean,
    val hasEqualizer: Boolean,
    val hasBassBoost: Boolean,
    val hasLoudnessEnhancer: Boolean,
    val hasSpatializer: Boolean,
    val canCreateTestEqualizer: Boolean
)

class AudioCapabilityScanner(private val context: Context) {

    fun scanCapabilities(): CapabilityReport {
        val hasDynamicsProcessing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val hasEqualizer = true
        val hasBassBoost = true
        val hasLoudnessEnhancer = true
        
        var canCreateTestEq = false
        try {
            val eq = Equalizer(0, 0)
            canCreateTestEq = true
            eq.release()
        } catch (e: Exception) {
            canCreateTestEq = false
        }

        var hasSpatializer = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val spatializer = audioManager.spatializer
            hasSpatializer = spatializer.isAvailable
        }

        return CapabilityReport(
            apiLevel = Build.VERSION.SDK_INT,
            hasDynamicsProcessing = hasDynamicsProcessing,
            hasEqualizer = hasEqualizer,
            hasBassBoost = hasBassBoost,
            hasLoudnessEnhancer = hasLoudnessEnhancer,
            hasSpatializer = hasSpatializer,
            canCreateTestEqualizer = canCreateTestEq
        )
    }
}
