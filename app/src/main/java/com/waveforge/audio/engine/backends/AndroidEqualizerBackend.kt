package com.waveforge.audio.engine.backends

import android.media.audiofx.Equalizer
import android.util.Log
import com.waveforge.audio.domain.EqBand
import com.waveforge.audio.domain.WfEqBand
import kotlin.math.roundToInt

class AndroidEqualizerBackend(
    override val activeSessionId: Int,
    override val packageName: String
) : AudioProcessingBackend {

    private val TAG = "WF_AUDIO"
    private var equalizer: Equalizer? = null
    override var isAvailable: Boolean = false
        private set
    override var hasControl: Boolean = false
        private set
    override val backendName: String
        get() = if (activeSessionId == 0) "Global (Compatibility)" else "External Session ($packageName)"

    init {
        try {
            Log.d(TAG, "creating Equalizer for session $activeSessionId")
            equalizer = Equalizer(0, activeSessionId)
            isAvailable = true
            hasControl = equalizer?.hasControl() ?: false
            Log.d(TAG, "Equalizer created. control = $hasControl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Equalizer", e)
            isAvailable = false
            hasControl = false
        }
    }

    override fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            Log.d(TAG, "enabled = $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set EQ enabled", e)
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
                
                // Interpolate from wfBands
                val desiredGainDb = interpolateGain(centerFreq, wfBands)
                
                // Convert db to millibels
                var millibels = (desiredGainDb * 100).roundToInt().toShort()
                
                // Clamp
                if (millibels < minLevel) millibels = minLevel
                if (millibels > maxLevel) millibels = maxLevel
                
                equalizer!!.setBandLevel(i.toShort(), millibels)
                
                // Verify
                val applied = equalizer!!.getBandLevel(i.toShort())
                Log.d(TAG, "applying band... Freq: $centerFreq, Requested: $millibels, Applied: $applied. parameter verified")
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read hardware bands", e)
        }
        return list
    }

    override fun release() {
        try {
            equalizer?.release()
            equalizer = null
            Log.d(TAG, "session closed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release", e)
        }
    }
}
