package com.waveforge.audio.engine.backends

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import com.waveforge.audio.engine.DspCapability
import com.waveforge.audio.engine.EqBandRequest
import kotlin.math.roundToInt

class ExternalSessionAudioBackend(val sessionId: Int) : AudioProcessingBackend {
    private val TAG = "WaveForgeExternalBackend"
    
    override val backendName = "Android AudioEffect (Session $sessionId)"
    override var isAttached: Boolean = false
        private set
        
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private val capabilities = mutableMapOf<String, DspCapability>()
    private var lastError: String? = null

    val hasEqControl: Boolean get() = equalizer?.hasControl() == true
    val hasBassControl: Boolean get() = bassBoost?.hasControl() == true
    val hasLoudnessControl: Boolean get() = loudnessEnhancer?.hasControl() == true
    var statePushSuccessful: Boolean = false

    
    private var nativeBands = 0
    private var minEq = -1500
    private var maxEq = 1500

    init {
        try {
            equalizer = Equalizer(0, sessionId).apply {
                val hasCtrl = hasControl()
                capabilities["EQ"] = if (hasCtrl) DspCapability.SUPPORTED_NATIVE else DspCapability.UNSUPPORTED_DEVICE
                
                if (hasCtrl) {
                    nativeBands = numberOfBands.toInt()
                    minEq = bandLevelRange[0].toInt()
                    maxEq = bandLevelRange[1].toInt()
                }
            }
            
            try {
                bassBoost = BassBoost(0, sessionId).apply {
                    capabilities["BassBoost"] = if (hasControl() && strengthSupported) DspCapability.SUPPORTED_NATIVE else DspCapability.UNSUPPORTED_DEVICE
                }
            } catch (e: Exception) {
                capabilities["BassBoost"] = DspCapability.UNSUPPORTED_DEVICE
            }
            
            try {
                loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                    capabilities["Loudness"] = if (hasControl()) DspCapability.SUPPORTED_NATIVE else DspCapability.UNSUPPORTED_DEVICE
                }
            } catch (e: Exception) {
                capabilities["Loudness"] = DspCapability.UNSUPPORTED_DEVICE
            }
            
            // Mark advanced features as unsupported for external sessions
            listOf("Haas", "Crossfeed", "PBE", "AFR", "Compressor", "Limiter", "Preamp", "ChannelConfig").forEach {
                capabilities[it] = DspCapability.UNSUPPORTED_EXTERNAL_SESSION
            }
            
            isAttached = true
            Log.d(TAG, "Attached to session $sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach to session $sessionId", e)
            lastError = e.message
            capabilities["EQ"] = DspCapability.ERROR_INITIALISING
            isAttached = false
            release()
        }
    }

    override fun getCapability(feature: String) = capabilities[feature] ?: DspCapability.UNAVAILABLE_NO_SESSION

    override fun setBassEnabled(enabled: Boolean) {
        try {
            bassBoost?.enabled = enabled
            Log.d(TAG, "setBassEnabled: $enabled (hasControl=${hasBassControl})")
        } catch (e: Exception) {
            lastError = "Bass error: ${e.message}"
            Log.e(TAG, "Error setting Bass enabled", e)
        }
    }

    override fun setBassStrength(strength: Int) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength.toShort())
                Log.d(TAG, "setBassStrength: $strength")
            }
        } catch (e: Exception) {
            lastError = "Bass strength error: ${e.message}"
            Log.e(TAG, "Error setting Bass strength", e)
        }
    }

    override fun setLoudnessEnabled(enabled: Boolean) {
        try {
            loudnessEnhancer?.enabled = enabled
            Log.d(TAG, "setLoudnessEnabled: $enabled (hasControl=${hasLoudnessControl})")
        } catch (e: Exception) {
            lastError = "Loudness error: ${e.message}"
            Log.e(TAG, "Error setting Loudness enabled", e)
        }
    }

    override fun setLoudnessGain(gain: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gain)
            Log.d(TAG, "setLoudnessGain: $gain")
        } catch (e: Exception) {
            lastError = "Loudness gain error: ${e.message}"
            Log.e(TAG, "Error setting Loudness gain", e)
        }
    }

    override fun setEqEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            Log.d(TAG, "setEqEnabled: $enabled (hasControl=${hasEqControl})")
        } catch (e: Exception) {
            lastError = "EQ error: ${e.message}"
            Log.e(TAG, "Error setting EQ enabled", e)
        }
    }

    override fun setEqBands(bands: List<EqBandRequest>) {
        if (equalizer == null || nativeBands == 0) return
        try {
            // Very simple interpolation mapping from 10 virtual bands to N native bands
            val nativeBandLevels = IntArray(nativeBands)
            
            for (i in 0 until nativeBands) {
                val centerFreq = equalizer!!.getCenterFreq(i.toShort())
                // Find nearest virtual band
                val nearest = bands.minByOrNull { Math.abs(it.centerFreq - centerFreq) }
                if (nearest != null) {
                    val targetGain = nearest.gain.coerceIn(minEq, maxEq)
                    equalizer!!.setBandLevel(i.toShort(), targetGain.toShort())
                }
            }
            Log.d(TAG, "setEqBands: pushed to native EQ")
        } catch (e: Exception) {
            lastError = "EQ band error: ${e.message}"
            Log.e(TAG, "Error setting EQ bands", e)
        }
    }

    override fun setHaasSurround(enabled: Boolean, delayMs: Int, balance: Int, fx1: Int, fx2: Int, sideOnly: Boolean, wetMix: Int) { }
    override fun setChannelConfig(mode: String, stereoWidth: Int) { }
    override fun setCrossfeed(mode: String, directGain: Int, crossGain: Int, hfAttenuation: Int, hfCutoff: Int) { }
    override fun setPerceptualBass(strength: Int, precut: Float) { }
    override fun setAuditoryFatigueReduction(mode: String) { }
    override fun setCompressor(enabled: Boolean, threshold: Int, makeupGain: Int, ratio: Int, knee: String, attackMs: Int, releaseMs: Int) { }
    override fun setLimiter(enabled: Boolean, threshold: Int) { }
    override fun setPreamp(gain: Int) { }

    override fun release() {
        equalizer?.release()
        bassBoost?.release()
        loudnessEnhancer?.release()
        equalizer = null
        bassBoost = null
        loudnessEnhancer = null
        isAttached = false
    }

    override fun getNativeEqBands(): Int = nativeBands
    override fun getLastError(): String? = lastError
}
