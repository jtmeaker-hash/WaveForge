package com.waveforge.audio.engine.backends

import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.DynamicsProcessing
import android.util.Log
import com.waveforge.audio.domain.EqBand
import com.waveforge.audio.domain.WfEqBand
import kotlin.math.roundToInt

open class AndroidAudioEffectBackend(
    override val activeSessionId: Int,
    override val packageName: String
) : AudioProcessingBackend {

    private val TAG = "WF_AUDIO"
    
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var virtualizer: Virtualizer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null
    
    override var isAvailable: Boolean = false
        protected set
    override var hasControl: Boolean = false
        protected set
    override val backendName: String
        get() = if (activeSessionId == 0) "Global AudioEffect" else "External AudioEffect ($packageName)"

    private var masterBypassed = false

    init {
        try {
            Log.d(TAG, "creating AudioEffects for session $activeSessionId")
            equalizer = Equalizer(0, activeSessionId)
            bassBoost = BassBoost(0, activeSessionId)
            loudnessEnhancer = LoudnessEnhancer(activeSessionId)
            virtualizer = Virtualizer(0, activeSessionId)
            
            try {
                val builder = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, true, 1, true, 1, false, 0, true
                )
                dynamicsProcessing = DynamicsProcessing(0, activeSessionId, builder.build())
            } catch (e: Exception) {
                Log.w(TAG, "DynamicsProcessing not supported on this device/session")
            }
            
            isAvailable = true
            hasControl = equalizer?.hasControl() ?: false
            Log.d(TAG, "Effects created. control = $hasControl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Effects", e)
            isAvailable = false
            hasControl = false
        }
    }

    override fun setMasterBypass(bypassed: Boolean) {
        masterBypassed = bypassed
        val enabled = !bypassed
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            loudnessEnhancer?.enabled = enabled
            virtualizer?.enabled = enabled
            dynamicsProcessing?.enabled = enabled
            Log.d(TAG, "Master Bypass = $bypassed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set bypass", e)
        }
    }

    override fun applyEqCurve(wfBands: List<WfEqBand>) {
        if (wfBands.isEmpty() || equalizer == null) return
        try {
            val numBands = equalizer!!.numberOfBands
            val minLevel = equalizer!!.bandLevelRange[0]
            val maxLevel = equalizer!!.bandLevelRange[1]
            
            for (i in 0 until numBands) {
                val centerFreq = equalizer!!.getCenterFreq(i.toShort())
                val desiredGainDb = interpolateGain(centerFreq, wfBands)
                var millibels = (desiredGainDb * 100).roundToInt().toShort()
                
                if (millibels < minLevel) millibels = minLevel
                if (millibels > maxLevel) millibels = maxLevel
                
                equalizer!!.setBandLevel(i.toShort(), millibels)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply EQ curve", e)
        }
    }

    private fun interpolateGain(freq: Int, wfBands: List<WfEqBand>): Float {
        val sorted = wfBands.sortedBy { it.centerFreq }
        if (freq <= sorted.first().centerFreq) return sorted.first().gainDb
        if (freq >= sorted.last().centerFreq) return sorted.last().gainDb
        
        for (i in 0 until sorted.size - 1) {
            val b1 = sorted[i]
            val b2 = sorted[i + 1]
            if (freq >= b1.centerFreq && freq <= b2.centerFreq) {
                val ratio = (freq - b1.centerFreq).toFloat() / (b2.centerFreq - b1.centerFreq)
                return b1.gainDb + ratio * (b2.gainDb - b1.gainDb)
            }
        }
        return 0f
    }

    override fun getHardwareBands(): List<EqBand> {
        val list = mutableListOf<EqBand>()
        if (equalizer == null) return list
        try {
            val numBands = equalizer!!.numberOfBands
            val minLevel = equalizer!!.bandLevelRange[0]
            val maxLevel = equalizer!!.bandLevelRange[1]
            for (i in 0 until numBands) {
                list.add(
                    EqBand(
                        index = i.toShort(),
                        centerFreq = equalizer!!.getCenterFreq(i.toShort()),
                        minGain = minLevel,
                        maxGain = maxLevel,
                        gain = equalizer!!.getBandLevel(i.toShort())
                    )
                )
            }
        } catch (e: Exception) {}
        return list
    }
    
    override fun setBassStrength(strength: Int) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength.toShort())
            }
        } catch (e: Exception) {}
    }
    
    override fun setLoudnessGain(gainMb: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainMb)
        } catch (e: Exception) {}
    }
    
    override fun setVirtualizerStrength(strength: Int) {
        try {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(strength.toShort())
            }
        } catch (e: Exception) {}
    }
    
    override fun setCompressor(enabled: Boolean, threshold: Float, ratio: Float) {
        // Mock implementation for DynamicsProcessing as it's complex
    }

    override fun setPitch(pitch: Float) {
        // Not supported in external session via AudioEffect
    }

    override fun setTempo(tempo: Float) {
        // Not supported in external session via AudioEffect
    }

    override fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
            virtualizer?.release()
            dynamicsProcessing?.release()
            equalizer = null
            Log.d(TAG, "AudioEffects released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release", e)
        }
    }
}
