package com.waveforge.audio.engine.backends

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.audiofx.DynamicsProcessing
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
    private var virtualizer: Virtualizer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null
    
    private val capabilities = mutableMapOf<String, DspCapability>()
    private var lastError: String? = null

    val hasEqControl: Boolean get() = equalizer?.hasControl() == true
    val hasBassControl: Boolean get() = bassBoost?.hasControl() == true
    val hasLoudnessControl: Boolean get() = loudnessEnhancer?.hasControl() == true
    var statePushSuccessful: Boolean = false

    private var nativeBands = 0
    private var minEq = -1500
    private var maxEq = 1500
    
    private var userEqBands: List<EqBandRequest> = emptyList()
    private var pbeStrength = 0
    private var afrMode = "Off"

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
                    // Use Loudness Enhancer for Preamp
                    capabilities["Preamp"] = capabilities["Loudness"]!!
                }
            } catch (e: Exception) {
                capabilities["Loudness"] = DspCapability.UNSUPPORTED_DEVICE
                capabilities["Preamp"] = DspCapability.UNSUPPORTED_DEVICE
            }
            
            try {
                virtualizer = Virtualizer(0, sessionId).apply {
                    capabilities["Stereo"] = if (hasControl() && strengthSupported) DspCapability.SUPPORTED_MAPPED else DspCapability.UNSUPPORTED_DEVICE
                }
            } catch (e: Exception) {
                capabilities["Stereo"] = DspCapability.UNSUPPORTED_DEVICE
            }
            
            try {
                // DynamicsProcessing requires Android 9 (API 28+)
                val cfg = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, // channels
                    false, 0, // preEq
                    true, 1, // mbc
                    false, 0, // postEq
                    true // limiter
                ).build()
                dynamicsProcessing = DynamicsProcessing(0, sessionId, cfg)
                capabilities["Compressor"] = if (dynamicsProcessing?.hasControl() == true) DspCapability.SUPPORTED_NATIVE else DspCapability.UNSUPPORTED_DEVICE
                capabilities["Limiter"] = capabilities["Compressor"]!!
            } catch (e: Exception) {
                capabilities["Compressor"] = DspCapability.UNSUPPORTED_DEVICE
                capabilities["Limiter"] = DspCapability.UNSUPPORTED_DEVICE
                Log.e(TAG, "Dynamics processing not available", e)
            }
            
            // Mapped Capabilities via EQ
            capabilities["Perceptual"] = capabilities["EQ"]?.let { if(it == DspCapability.SUPPORTED_NATIVE) DspCapability.SUPPORTED_MAPPED else it } ?: DspCapability.UNSUPPORTED_DEVICE
            capabilities["Auditory"] = capabilities["Perceptual"]!!
            
            // Unsupportable without root/PCM
            listOf("Haas", "Crossfeed", "Channel").forEach {
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
        userEqBands = bands
        applyCombinedEq()
    }
    
    private fun applyCombinedEq() {
        if (equalizer == null || nativeBands == 0 || userEqBands.isEmpty()) return
        try {
            val nativeBandLevels = IntArray(nativeBands)
            
            for (i in 0 until nativeBands) {
                val centerFreq = equalizer!!.getCenterFreq(i.toShort())
                val nearest = userEqBands.minByOrNull { Math.abs(it.centerFreq - centerFreq) }
                if (nearest != null) {
                    var targetGain = nearest.gain
                    
                    // Apply Mapped PBE (Boost Lows)
                    if (pbeStrength > 0 && centerFreq < 150000) {
                        targetGain += (pbeStrength * 10) // up to +500 mB
                    }
                    
                    // Apply Mapped AFR (Cut Highs)
                    if (afrMode != "Off" && centerFreq > 4000000) {
                        val offset = if (afrMode == "Strong") -600 else -300
                        targetGain += offset
                    }
                    
                    targetGain = targetGain.coerceIn(minEq, maxEq)
                    equalizer!!.setBandLevel(i.toShort(), targetGain.toShort())
                }
            }
            Log.d(TAG, "setEqBands: pushed combined EQ (PBE=$pbeStrength, AFR=$afrMode)")
        } catch (e: Exception) {
            lastError = "EQ band error: ${e.message}"
            Log.e(TAG, "Error setting EQ bands", e)
        }
    }

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
                bassBoost?.setStrength((strength * 10).toShort()) // Map 0-100 to 0-1000
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
    
    // Alias preamp to LoudnessEnhancer for external session mode
    override fun setPreamp(gain: Int) {
        setLoudnessGain(gain)
    }

    override fun setChannelConfig(mode: String, stereoWidth: Int) {
        try {
            virtualizer?.enabled = (mode == "StereoWidth")
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength((stereoWidth * 10).toShort())
            }
            Log.d(TAG, "setVirtualizer: enabled=${mode == "StereoWidth"} strength=$stereoWidth")
        } catch (e: Exception) {
            lastError = "Virtualizer error: ${e.message}"
            Log.e(TAG, "Error setting Virtualizer", e)
        }
    }

    override fun setPerceptualBass(strength: Int, precut: Float) {
        pbeStrength = strength
        applyCombinedEq()
    }

    override fun setAuditoryFatigueReduction(mode: String) {
        afrMode = mode
        applyCombinedEq()
    }

    override fun setCompressor(enabled: Boolean, threshold: Int, makeupGain: Int, ratio: Int, knee: String, attackMs: Int, releaseMs: Int) {
        try {
            val dp = dynamicsProcessing ?: return
            
            // For channels 0 and 1
            for (ch in 0..1) {
                val mbc = dp.getMbcBandByChannelIndex(ch, 0)
                if (mbc != null) {
                    mbc.isEnabled = enabled
                    mbc.attackTime = attackMs.toFloat()
                    mbc.releaseTime = releaseMs.toFloat()
                    mbc.ratio = ratio.toFloat()
                    mbc.threshold = threshold.toFloat()
                    mbc.postGain = makeupGain.toFloat()
                    dp.setMbcBandByChannelIndex(ch, 0, mbc)
                }
            }
            Log.d(TAG, "setCompressor: enabled=$enabled")
        } catch(e: Exception) {
            lastError = "Compressor error: ${e.message}"
        }
    }

    override fun setLimiter(enabled: Boolean, threshold: Int) {
        try {
            val dp = dynamicsProcessing ?: return
            
            for (ch in 0..1) {
                val lim = dp.getLimiterByChannelIndex(ch)
                if (lim != null) {
                    lim.isEnabled = enabled
                    lim.linkGroup = 0
                    lim.attackTime = 1.0f
                    lim.releaseTime = 50.0f
                    lim.ratio = 10.0f
                    lim.threshold = threshold.toFloat()
                    dp.setLimiterByChannelIndex(ch, lim)
                }
            }
            Log.d(TAG, "setLimiter: enabled=$enabled")
        } catch(e: Exception) {
            lastError = "Limiter error: ${e.message}"
        }
    }

    override fun setHaasSurround(enabled: Boolean, delayMs: Int, balance: Int, fx1: Int, fx2: Int, sideOnly: Boolean, wetMix: Int) { }
    override fun setCrossfeed(mode: String, directGain: Int, crossGain: Int, hfAttenuation: Int, hfCutoff: Int) { }

    override fun release() {
        equalizer?.release()
        bassBoost?.release()
        loudnessEnhancer?.release()
        virtualizer?.release()
        dynamicsProcessing?.release()
        
        equalizer = null
        bassBoost = null
        loudnessEnhancer = null
        virtualizer = null
        dynamicsProcessing = null
        
        isAttached = false
    }

    override fun getNativeEqBands(): Int = nativeBands
    override fun getLastError(): String? = lastError
}
