package com.waveforge.audio.engine.backends

import com.waveforge.audio.engine.DspCapability
import com.waveforge.audio.engine.EqBandRequest
import kotlinx.coroutines.flow.StateFlow

interface AudioProcessingBackend {
    val isAttached: Boolean
    val backendName: String
    
    fun getCapability(feature: String): DspCapability
    
    fun setEqEnabled(enabled: Boolean)
    fun setEqBands(bands: List<EqBandRequest>)
    
    fun setBassEnabled(enabled: Boolean)
    fun setBassStrength(strength: Int)
    
    fun setLoudnessEnabled(enabled: Boolean)
    fun setLoudnessGain(gain: Int)
    
    // Phase 3-5 additions
    fun setHaasSurround(enabled: Boolean, delayMs: Int, balance: Int, fx1: Int, fx2: Int, sideOnly: Boolean, wetMix: Int)
    fun setChannelConfig(mode: String, stereoWidth: Int)
    fun setCrossfeed(mode: String, directGain: Int, crossGain: Int, hfAttenuation: Int, hfCutoff: Int)
    fun setPerceptualBass(strength: Int, precut: Float)
    fun setAuditoryFatigueReduction(mode: String)
    fun setCompressor(enabled: Boolean, threshold: Int, makeupGain: Int, ratio: Int, knee: String, attackMs: Int, releaseMs: Int)
    fun setLimiter(enabled: Boolean, threshold: Int)
    
    fun setPreamp(gain: Int)
    
    fun release()
    
    fun getNativeEqBands(): Int
    fun getLastError(): String?
}
