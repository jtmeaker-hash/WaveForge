package com.waveforge.audio.engine

import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.AudioEffect
import android.util.Log
import com.waveforge.audio.domain.EqBand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class EngineState {
    object Disabled : EngineState()
    object WaitingForSession : EngineState()
    data class Attached(val sessionId: Int, val packageName: String?) : EngineState()
    data class Error(val message: String) : EngineState()
}

class AudioEngine(private val context: Context) {
    private val TAG = "WaveForgeEngine"
    
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private val _engineState = MutableStateFlow<EngineState>(EngineState.WaitingForSession)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _activeSessionId = MutableStateFlow<Int?>(null)
    val activeSessionId: StateFlow<Int?> = _activeSessionId.asStateFlow()

    private val _activePackageName = MutableStateFlow<String?>(null)
    val activePackageName: StateFlow<String?> = _activePackageName.asStateFlow()
    
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

    // Diagnostic flows
    val eqCreated = MutableStateFlow(false)
    val eqHasControl = MutableStateFlow(false)
    val bassCreated = MutableStateFlow(false)
    val bassHasControl = MutableStateFlow(false)
    val bassStrengthSupported = MutableStateFlow(false)
    val loudnessCreated = MutableStateFlow(false)
    val loudnessHasControl = MutableStateFlow(false)
    val lastSessionEvent = MutableStateFlow("None")
    
    fun attachToSession(sessionId: Int, packageName: String?) {
        lastSessionEvent.value = "OPEN session=$sessionId package=$packageName"
        try {
            Log.d(TAG, "attaching Equalizer to session $sessionId")
            release()
            
            equalizer = Equalizer(0, sessionId).apply {
                enabled = _eqEnabled.value
                val hasCtrl = hasControl()
                eqHasControl.value = hasCtrl
                if (!hasCtrl) {
                    _lastError.value = "Equalizer created but WaveForge does not have control."
                }
                
                setControlStatusListener { effect, controlGranted ->
                    eqHasControl.value = controlGranted
                }
            }
            eqCreated.value = true
            
            val bands = mutableListOf<EqBand>()
            val numBands = equalizer?.numberOfBands ?: 0
            val minEQLevel = equalizer?.bandLevelRange?.get(0) ?: 0
            val maxEQLevel = equalizer?.bandLevelRange?.get(1) ?: 0
            
            Log.d(TAG, "EQ bands=$numBands range=$minEQLevel..$maxEQLevel")
            
            for (i in 0 until numBands) {
                val centerFreq = equalizer?.getCenterFreq(i.toShort()) ?: 0
                val gain = equalizer?.getBandLevel(i.toShort()) ?: 0
                bands.add(EqBand(i.toShort(), centerFreq, minEQLevel, maxEQLevel, gain))
            }
            _eqBands.value = bands

            try {
                bassBoost = BassBoost(0, sessionId).apply {
                    enabled = _bassEnabled.value
                    bassHasControl.value = hasControl()
                    bassStrengthSupported.value = strengthSupported
                    Log.d(TAG, "BassBoost supported=$strengthSupported")
                    if (strengthSupported) {
                        setStrength(_bassStrength.value.toShort())
                    }
                    
                    setControlStatusListener { effect, controlGranted ->
                        bassHasControl.value = controlGranted
                    }
                }
                bassCreated.value = true
            } catch (e: Exception) {
                Log.w(TAG, "BassBoost not supported", e)
                bassCreated.value = false
            }
            
            try {
                loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                    enabled = _loudnessEnabled.value
                    loudnessHasControl.value = hasControl()
                    setTargetGain(_loudnessGain.value)
                    Log.d(TAG, "LoudnessEnhancer created")
                }
                loudnessCreated.value = true
            } catch (e: Exception) {
                Log.w(TAG, "LoudnessEnhancer not supported", e)
                loudnessCreated.value = false
            }
            
            _activeSessionId.value = sessionId
            _activePackageName.value = packageName
            _engineState.value = EngineState.Attached(sessionId, packageName)
            Log.d(TAG, "session $sessionId ACTIVE")
            
        } catch (e: Exception) {
            Log.e(TAG, "Engine initialization failed for session $sessionId", e)
            _engineState.value = EngineState.Error(e.message ?: "Unknown error")
            _lastError.value = e.message
            release()
        }
    }

    fun detachSession(sessionId: Int? = null) {
        if (sessionId == null || sessionId == _activeSessionId.value) {
            lastSessionEvent.value = "CLOSE session=${sessionId ?: "all"}"
            Log.d(TAG, "Detaching session ${sessionId ?: "all"}")
            release()
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing effects", e)
        } finally {
            equalizer = null
            bassBoost = null
            loudnessEnhancer = null
            
            eqCreated.value = false
            bassCreated.value = false
            loudnessCreated.value = false
            
            _activeSessionId.value = null
            _activePackageName.value = null
            _engineState.value = EngineState.WaitingForSession
        }
    }
    
    fun setEqEnabled(enabled: Boolean) {
        try {
            _eqEnabled.value = enabled
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            _lastError.value = "Failed to set EQ state: ${e.message}"
        }
    }
    
    fun setBandLevel(bandIndex: Short, level: Short) {
        try {
            // Clamp level to valid range
            val minEQLevel = equalizer?.bandLevelRange?.get(0) ?: -1500
            val maxEQLevel = equalizer?.bandLevelRange?.get(1) ?: 1500
            val clampedLevel = level.coerceIn(minEQLevel, maxEQLevel)

            equalizer?.setBandLevel(bandIndex, clampedLevel)
            
            // Read back actual
            val actual = equalizer?.getBandLevel(bandIndex) ?: clampedLevel
            val updated = _eqBands.value.map { if (it.index == bandIndex) it.copy(gain = actual) else it }
            _eqBands.value = updated
        } catch (e: Exception) {
            _lastError.value = "Failed to set band level: ${e.message}"
        }
    }
    
    fun setBassEnabled(enabled: Boolean) {
        try {
            _bassEnabled.value = enabled
            bassBoost?.enabled = enabled
        } catch (e: Exception) {
            _lastError.value = "Failed to set Bass Boost state: ${e.message}"
        }
    }
    
    fun setBassStrength(strength: Int) {
        try {
            val clamped = strength.coerceIn(0, 1000)
            _bassStrength.value = clamped
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(clamped.toShort())
            }
        } catch (e: Exception) {
            _lastError.value = "Failed to set Bass strength: ${e.message}"
        }
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        try {
            _loudnessEnabled.value = enabled
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            _lastError.value = "Failed to set Loudness state: ${e.message}"
        }
    }

    fun setLoudnessGain(gain: Int) {
        try {
            _loudnessGain.value = gain
            loudnessEnhancer?.setTargetGain(gain)
        } catch (e: Exception) {
            _lastError.value = "Failed to set Loudness gain: ${e.message}"
        }
    }
    
    fun resetEq() {
        _eqBands.value.forEach {
            setBandLevel(it.index, 0)
        }
    }
    
    fun applyExtremeTest() {
        if (_eqBands.value.isEmpty()) return
        
        val minEQLevel = equalizer?.bandLevelRange?.get(0) ?: -1500
        val maxEQLevel = equalizer?.bandLevelRange?.get(1) ?: 1500
        
        val lowestBand = _eqBands.value.minByOrNull { it.centerFreq }
        val highestBand = _eqBands.value.maxByOrNull { it.centerFreq }
        
        lowestBand?.let { setBandLevel(it.index, minEQLevel) }
        highestBand?.let { setBandLevel(it.index, maxEQLevel) }
    }
}
