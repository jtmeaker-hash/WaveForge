package com.waveforge.audio.engine.pcm

import android.util.Log
import com.waveforge.audio.engine.DspCapability
import com.waveforge.audio.engine.EqBandRequest
import com.waveforge.audio.engine.HaasConfig
import com.waveforge.audio.engine.CrossfeedConfig
import com.waveforge.audio.engine.backends.AudioProcessingBackend

class PcmDspBackend(
    private val haasProcessor: HaasAudioProcessor,
    private val crossfeedProcessor: CrossfeedAudioProcessor
) : AudioProcessingBackend {
    private val TAG = "WaveForgePcmBackend"
    override val backendName = "WaveForge PCM Engine"
    override var isAttached: Boolean = true
        private set
        
    private var lastError: String? = null
    
    override fun getCapability(feature: String): DspCapability {
        return when (feature) {
            "Haas", "Crossfeed" -> DspCapability.SUPPORTED_PCM_ONLY
            else -> DspCapability.UNSUPPORTED_DEVICE
        }
    }
    
    override fun setEqEnabled(enabled: Boolean) { }
    override fun setEqBands(bands: List<EqBandRequest>) { }
    override fun setBassEnabled(enabled: Boolean) { }
    override fun setBassStrength(strength: Int) { }
    override fun setLoudnessEnabled(enabled: Boolean) { }
    override fun setLoudnessGain(gain: Int) { }
    
    override fun setHaasSurround(enabled: Boolean, delayMs: Int, balance: Int, fx1: Int, fx2: Int, sideOnly: Boolean, wetMix: Int) {
        Log.d(TAG, "Haas enabled=$enabled delay=$delayMs ms delayedChannel=Right width=$wetMix amount=$balance")
        haasProcessor.setConfig(HaasConfig(
            enabled = enabled,
            delayMs = delayMs,
            amount = balance,
            width = wetMix,
            delayedChannel = "Right" // We default to right if the parameters are limited
        ))
    }
    
    fun setHaasSurroundConfig(config: HaasConfig) {
        Log.d(TAG, "Haas enabled=${config.enabled} delay=${config.delayMs} ms delayedChannel=${config.delayedChannel}")
        haasProcessor.setConfig(config)
    }

    override fun setChannelConfig(mode: String, stereoWidth: Int) { }
    
    override fun setCrossfeed(mode: String, directGain: Int, crossGain: Int, hfAttenuation: Int, hfCutoff: Int) {
        val enabled = mode != "Off"
        // Wait, AudioProcessingBackend interface doesn't give us the amount easily, let's just make a specific setter for config
    }
    
    fun setCrossfeedConfig(config: CrossfeedConfig) {
        Log.d(TAG, "Crossfeed enabled=${config.enabled} amount=${config.amount} cutoff=${config.cutoffHz} Hz")
        crossfeedProcessor.setConfig(config)
    }
    
    override fun setPerceptualBass(strength: Int, precut: Float) { }
    override fun setAuditoryFatigueReduction(mode: String) { }
    override fun setCompressor(enabled: Boolean, threshold: Int, makeupGain: Int, ratio: Int, knee: String, attackMs: Int, releaseMs: Int) { }
    override fun setLimiter(enabled: Boolean, threshold: Int) { }
    override fun setPreamp(gain: Int) { }

    override fun release() {
        isAttached = false
    }

    override fun getNativeEqBands(): Int = 10
    override fun getLastError(): String? = lastError
}
