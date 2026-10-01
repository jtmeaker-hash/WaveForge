package com.waveforge.audio.engine

import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.waveforge.audio.domain.EqBand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngine(private val context: Context) {
    private val TAG = "WaveForgeEngine"
    
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private val _engineState = MutableStateFlow("Disconnected")
    val engineState: StateFlow<String> = _engineState.asStateFlow()
    
    private val _eqBands = MutableStateFlow<List<EqBand>>(emptyList())
    val eqBands: StateFlow<List<EqBand>> = _eqBands.asStateFlow()
    
    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _bassEnabled = MutableStateFlow(false)
    val bassEnabled: StateFlow<Boolean> = _bassEnabled.asStateFlow()
    
    private val _bassStrength = MutableStateFlow(0)
    val bassStrength: StateFlow<Int> = _bassStrength.asStateFlow()

    private val _loudnessEnabled = MutableStateFlow(false)
    val loudnessEnabled: StateFlow<Boolean> = _loudnessEnabled.asStateFlow()

    private val _loudnessGain = MutableStateFlow(0)
    val loudnessGain: StateFlow<Int> = _loudnessGain.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    init {
        initEngine(0) // 0 for global mix, though deprecated, works on some devices or requires permissions.
    }
    
    fun initEngine(sessionId: Int) {
        try {
            Log.d(TAG, "Initializing AudioEngine with sessionId $sessionId")
            equalizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
            
            equalizer = Equalizer(0, sessionId).apply {
                enabled = _eqEnabled.value
            }
            
            val bands = mutableListOf<EqBand>()
            val numBands = equalizer?.numberOfBands ?: 0
            val minEQLevel = equalizer?.bandLevelRange?.get(0) ?: 0
            val maxEQLevel = equalizer?.bandLevelRange?.get(1) ?: 0
            
            for (i in 0 until numBands) {
                val centerFreq = equalizer?.getCenterFreq(i.toShort()) ?: 0
                val gain = equalizer?.getBandLevel(i.toShort()) ?: 0
                bands.add(EqBand(i.toShort(), centerFreq, minEQLevel, maxEQLevel, gain))
            }
            _eqBands.value = bands

            bassBoost = BassBoost(0, sessionId).apply {
                enabled = _bassEnabled.value
                if (strengthSupported) {
                    setStrength(_bassStrength.value.toShort())
                }
            }
            
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = _loudnessEnabled.value
                setTargetGain(_loudnessGain.value)
            }
            
            _engineState.value = "Connected"
            _lastError.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Engine initialization failed", e)
            _engineState.value = "Error initializing engine"
            _lastError.value = e.message
        }
    }
    
    fun setEqEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            _eqEnabled.value = enabled
            Log.d(TAG, "EQ enabled set to $enabled")
        } catch (e: Exception) {
            _lastError.value = "Failed to set EQ state: ${e.message}"
            Log.e(TAG, "Failed to set EQ state", e)
        }
    }
    
    fun setBandLevel(bandIndex: Short, level: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, level)
            val updated = _eqBands.value.map { if (it.index == bandIndex) it.copy(gain = level) else it }
            _eqBands.value = updated
        } catch (e: Exception) {
            _lastError.value = "Failed to set band level: ${e.message}"
            Log.e(TAG, "Failed to set band level", e)
        }
    }
    
    fun setBassEnabled(enabled: Boolean) {
        try {
            bassBoost?.enabled = enabled
            _bassEnabled.value = enabled
            Log.d(TAG, "Bass Boost enabled set to $enabled")
        } catch (e: Exception) {
            _lastError.value = "Failed to set Bass Boost state: ${e.message}"
        }
    }
    
    fun setBassStrength(strength: Int) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength.toShort())
                _bassStrength.value = strength
            }
        } catch (e: Exception) {
            _lastError.value = "Failed to set Bass strength: ${e.message}"
        }
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        try {
            loudnessEnhancer?.enabled = enabled
            _loudnessEnabled.value = enabled
            Log.d(TAG, "Loudness enabled set to $enabled")
        } catch (e: Exception) {
            _lastError.value = "Failed to set Loudness state: ${e.message}"
        }
    }

    fun setLoudnessGain(gain: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gain)
            _loudnessGain.value = gain
        } catch (e: Exception) {
            _lastError.value = "Failed to set Loudness gain: ${e.message}"
        }
    }
    
    fun resetEq() {
        _eqBands.value.forEach {
            setBandLevel(it.index, 0)
        }
    }
}
