package com.waveforge.audio.engine.pcm

import com.waveforge.audio.engine.DspCapability
import com.waveforge.audio.engine.EqBandRequest
import com.waveforge.audio.engine.backends.AudioProcessingBackend

class PcmDspBackend : AudioProcessingBackend {
    override val backendName = "WaveForge PCM Engine"
    override var isAttached: Boolean = true
        private set
        
    private var lastError: String? = null
    
    // In a real implementation, this would contain AudioProcessor instances
    // wired into ExoPlayer or an AudioTrack wrapper.
    
    override fun getCapability(feature: String): DspCapability {
        return DspCapability.SUPPORTED_PCM_ONLY
    }
    
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
        isAttached = false
    }

    override fun getNativeEqBands(): Int = 10
    override fun getLastError(): String? = lastError
}
