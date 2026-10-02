package com.waveforge.audio.engine.backends

import android.util.Log
import com.waveforge.audio.engine.DspCapability
import com.waveforge.audio.engine.EqBandRequest

class SystemRootAudioBackend : AudioProcessingBackend {
    private val TAG = "WaveForgeRootBackend"
    
    override val backendName = "System-Wide DSP Engine (Native)"
    
    // We do NOT have a real C++ root audio module running right now.
    // So this backend must truthfully report itself as unattached and its capabilities as unavailable.
    override var isAttached: Boolean = false
        private set
        
    private val capabilities = mutableMapOf<String, DspCapability>()
    private var lastError: String? = "Native system DSP module not found"

    init {
        Log.d(TAG, "Initializing Native System-Wide DSP Engine...")
        // Because the C++ daemon isn't running in this iteration, these are NOT supported.
        listOf("EQ", "BassBoost", "Loudness", "Haas", "Crossfeed", "PBE", "AFR", "Compressor", "Limiter", "Preamp", "ChannelConfig").forEach {
            capabilities[it] = DspCapability.UNAVAILABLE_NO_SESSION
        }
    }

    override fun getCapability(feature: String) = capabilities[feature] ?: DspCapability.UNAVAILABLE_NO_SESSION
    
    // Setters are no-ops because we are not attached
    override fun setEqEnabled(enabled: Boolean) { }
    override fun setEqBands(bands: List<EqBandRequest>) { }
    override fun setBassEnabled(enabled: Boolean) { }
    override fun setBassStrength(strength: Int) { }
    override fun setLoudnessEnabled(enabled: Boolean) { }
    override fun setLoudnessGain(gain: Int) { }
    
    override fun setHaasSurround(enabled: Boolean, delayMs: Int, balance: Int, fx1: Int, fx2: Int, sideOnly: Boolean, wetMix: Int) { }
    override fun setChannelConfig(mode: String, stereoWidth: Int) { }
    override fun setCrossfeed(mode: String, directGain: Int, crossGain: Int, hfAttenuation: Int, hfCutoff: Int) { }
    override fun setPerceptualBass(strength: Int, precut: Float) { }
    override fun setAuditoryFatigueReduction(mode: String) { }
    override fun setCompressor(enabled: Boolean, threshold: Int, makeupGain: Int, ratio: Int, knee: String, attackMs: Int, releaseMs: Int) { }
    override fun setLimiter(enabled: Boolean, threshold: Int) { }
    override fun setPreamp(gain: Int) { }

    override fun release() {
        Log.d(TAG, "Releasing System-Wide DSP Engine")
        isAttached = false
    }

    override fun getNativeEqBands(): Int = 0
    override fun getLastError(): String? = lastError
}
