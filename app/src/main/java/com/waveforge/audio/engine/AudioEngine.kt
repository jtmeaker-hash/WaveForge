package com.waveforge.audio.engine

import android.content.Context
import android.util.Log
import com.waveforge.audio.engine.backends.AudioProcessingBackend
import com.waveforge.audio.engine.backends.ExternalSessionAudioBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngine(private val context: Context) {
    private val TAG = "WaveForgeEngine"
    
    private var backend: AudioProcessingBackend? = null
    
    private val _engineState = MutableStateFlow<EngineState>(EngineState.WaitingForSession)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _activeSessionId = MutableStateFlow<Int?>(null)
    val activeSessionId: StateFlow<Int?> = _activeSessionId.asStateFlow()

    private val _activePackageName = MutableStateFlow<String?>(null)
    val activePackageName: StateFlow<String?> = _activePackageName.asStateFlow()
    
    private val _eqBands = MutableStateFlow<List<EqBandRequest>>(emptyList())
    val eqBands: StateFlow<List<EqBandRequest>> = _eqBands.asStateFlow()
    
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

    val lastSessionEvent = MutableStateFlow("None")
    val diagnosticsInfo = MutableStateFlow(DiagnosticsInfo())
    
    init {
        // Initialize 10 flat virtual bands
        val defaultBands = listOf(
            31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000
        ).mapIndexed { index, freq ->
            EqBandRequest(index.toShort(), freq * 1000, -1500, 1500, 0)
        }
        _eqBands.value = defaultBands
    }
    
    fun attachToSession(sessionId: Int, packageName: String?) {
        lastSessionEvent.value = "OPEN session=$sessionId package=$packageName"
        try {
            Log.d(TAG, "attaching ExternalSessionAudioBackend to session $sessionId")
            release()
            
            val newBackend = ExternalSessionAudioBackend(sessionId)
            if (newBackend.isAttached) {
                backend = newBackend
                
                // Reapply current states
                newBackend.setEqEnabled(_eqEnabled.value)
                newBackend.setEqBands(_eqBands.value)
                newBackend.setBassEnabled(_bassEnabled.value)
                newBackend.setBassStrength(_bassStrength.value)
                newBackend.setLoudnessEnabled(_loudnessEnabled.value)
                newBackend.setLoudnessGain(_loudnessGain.value)
                
                _activeSessionId.value = sessionId
                _activePackageName.value = packageName
                _engineState.value = EngineState.Attached(sessionId, packageName, "External")
                
                updateDiagnostics()
                Log.d(TAG, "session $sessionId ACTIVE")
            } else {
                _lastError.value = "Failed to attach backend"
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Engine initialization failed for session $sessionId", e)
            _engineState.value = EngineState.Error(e.message ?: "Unknown error")
            _lastError.value = e.message
            release()
        }
    }

    private fun updateDiagnostics() {
        val b = backend
        if (b != null) {
            val caps = mutableMapOf<String, DspCapability>()
            listOf("EQ", "BassBoost", "Loudness", "Haas", "Crossfeed", "PBE", "AFR", "Compressor", "Limiter", "Preamp", "ChannelConfig").forEach {
                caps[it] = b.getCapability(it)
            }
            diagnosticsInfo.value = DiagnosticsInfo(
                processingMode = b.backendName,
                activeSessionId = _activeSessionId.value,
                activePackage = _activePackageName.value,
                nativeEqBandCount = b.getNativeEqBands(),
                capabilities = caps,
                errors = listOfNotNull(b.getLastError())
            )
        } else {
            diagnosticsInfo.value = DiagnosticsInfo()
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
            backend?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing effects", e)
        } finally {
            backend = null
            _activeSessionId.value = null
            _activePackageName.value = null
            _engineState.value = EngineState.WaitingForSession
            updateDiagnostics()
        }
    }
    
    fun setEqEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
        backend?.setEqEnabled(enabled)
    }
    
    fun setBandLevel(bandIndex: Short, level: Short) {
        val updated = _eqBands.value.map { if (it.index == bandIndex) it.copy(gain = level.toInt()) else it }
        _eqBands.value = updated
        backend?.setEqBands(updated)
    }
    
    fun setBassEnabled(enabled: Boolean) {
        _bassEnabled.value = enabled
        backend?.setBassEnabled(enabled)
    }
    
    fun setBassStrength(strength: Int) {
        _bassStrength.value = strength
        backend?.setBassStrength(strength)
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        _loudnessEnabled.value = enabled
        backend?.setLoudnessEnabled(enabled)
    }

    fun setLoudnessGain(gain: Int) {
        _loudnessGain.value = gain
        backend?.setLoudnessGain(gain)
    }
    
    fun resetEq() {
        val updated = _eqBands.value.map { it.copy(gain = 0) }
        _eqBands.value = updated
        backend?.setEqBands(updated)
    }
    
    fun applyExtremeTest() {
        if (_eqBands.value.isEmpty()) return
        
        val lowestBand = _eqBands.value.minByOrNull { it.centerFreq }
        val highestBand = _eqBands.value.maxByOrNull { it.centerFreq }
        
        val updated = _eqBands.value.map {
            when (it.index) {
                lowestBand?.index -> it.copy(gain = -1500)
                highestBand?.index -> it.copy(gain = 1500)
                else -> it
            }
        }
        _eqBands.value = updated
        backend?.setEqBands(updated)
    }
}
