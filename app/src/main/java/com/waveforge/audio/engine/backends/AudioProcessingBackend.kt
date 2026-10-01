package com.waveforge.audio.engine.backends

import com.waveforge.audio.domain.EqBand
import com.waveforge.audio.domain.WfEqBand

interface AudioProcessingBackend {
    val isAvailable: Boolean
    val hasControl: Boolean
    val activeSessionId: Int
    val backendName: String
    val packageName: String

    fun setMasterBypass(bypassed: Boolean)
    fun applyEqCurve(wfBands: List<WfEqBand>)
    fun getHardwareBands(): List<EqBand>
    
    // Dynamics & Tone
    fun setBassStrength(strength: Int)
    fun setLoudnessGain(gainMb: Int)
    fun setVirtualizerStrength(strength: Int)
    fun setCompressor(enabled: Boolean, threshold: Float, ratio: Float)
    
    // Playback (Full DSP only)
    fun setPitch(pitch: Float)
    fun setTempo(tempo: Float)
    
    fun release()
}
