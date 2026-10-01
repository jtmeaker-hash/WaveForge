package com.waveforge.audio.engine

import android.content.Context
import android.util.Log
import com.waveforge.audio.engine.backends.AudioProcessingBackend
import com.waveforge.audio.engine.backends.ExternalSessionAudioBackend
import com.waveforge.audio.engine.backends.SystemRootAudioBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    
    private val _dspState = MutableStateFlow(DspState())
    val dspState: StateFlow<DspState> = _dspState.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    val lastSessionEvent = MutableStateFlow("None")
    val diagnosticsInfo = MutableStateFlow(DiagnosticsInfo())
    
    init {
        val defaultBands = listOf(
            31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000
        ).mapIndexed { index, freq ->
            EqBandRequest(index.toShort(), freq * 1000, -1500, 1500, 0)
        }
        _eqBands.value = defaultBands
        
        Log.d(TAG, "AudioEngine initialized. Trying Session-0 Fallback...")
        // Start with a session-0 global fallback attempt.
        attachToSession(0, "Global Mix (Session 0)")
    }

    private fun checkNativeSystemDsp(): Boolean {
        // Here we would probe for the Magisk/C++ daemon. 
        // Currently we know it doesn't exist.
        return false
    }

    fun attachToSession(sessionId: Int, packageName: String?) {
        Log.d(TAG, "OPEN session request: session=$sessionId package=$packageName")
        lastSessionEvent.value = "OPEN session=$sessionId package=$packageName"
        
        if (checkNativeSystemDsp()) {
            Log.d(TAG, "Using System-Wide DSP. Ignoring Android AudioEffect session $sessionId")
            val sysBackend = SystemRootAudioBackend()
            if (sysBackend.isAttached) {
                backend = sysBackend
                _engineState.value = EngineState.Attached(null, "System-Wide Mixer", "System DSP")
                applyCurrentStateToBackend(backend)
                updateDiagnostics()
                return
            }
        }

        try {
            Log.d(TAG, "Attaching ExternalSessionAudioBackend to session $sessionId")
            // Don't release if we are attaching to the exact same session
            if (_activeSessionId.value == sessionId && backend?.isAttached == true) {
                Log.d(TAG, "Already attached to session $sessionId. Skipping re-creation.")
                return
            }
            
            release()
            
            val newBackend = ExternalSessionAudioBackend(sessionId)
            if (newBackend.isAttached) {
                Log.d(TAG, "Backend successfully attached to session $sessionId")
                backend = newBackend
                _activeSessionId.value = sessionId
                _activePackageName.value = packageName
                _engineState.value = EngineState.Attached(sessionId, packageName, if (sessionId == 0) "Global Fallback" else "Compatibility Mode")
                
                applyCurrentStateToBackend(newBackend)
                updateDiagnostics()
            } else {
                Log.w(TAG, "Backend rejected attachment to session $sessionId")
                _lastError.value = newBackend.getLastError() ?: "Failed to attach backend"
                release()
                
                // If it was a real session that failed, fall back to global mix
                if (sessionId != 0) {
                    Log.d(TAG, "Attempting session-0 fallback after session $sessionId failed")
                    attachToSession(0, "Global Mix Fallback")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Engine initialization failed for session $sessionId", e)
            _engineState.value = EngineState.Error(e.message ?: "Unknown error")
            _lastError.value = e.message
            release()
        }
    }

    private fun applyCurrentStateToBackend(b: AudioProcessingBackend?) {
        if (b == null) return
        Log.d(TAG, "applyCurrentStateToBackend: Pushing all state to ${b.backendName}")
        
        val bypass = !_dspState.value.masterEnabled
        Log.d(TAG, "Master Enabled: ${!bypass}")
        
        // Push bypass/enabled states based on masterEnabled
        b.setEqEnabled(!bypass && _eqEnabled.value)
        b.setEqBands(_eqBands.value)
        
        b.setBassEnabled(!bypass && _bassEnabled.value)
        b.setBassStrength(_bassStrength.value)
        
        b.setLoudnessEnabled(!bypass && _loudnessEnabled.value)
        b.setLoudnessGain(_loudnessGain.value)
        
        val haas = _dspState.value.haas
        b.setHaasSurround(!bypass && haas.enabled, haas.delayMs, haas.amount, 0, 0, false, haas.width)
        
        val cf = _dspState.value.crossfeed
        b.setCrossfeed("Custom", cf.directLevel, cf.crossfeedLevel, 0, cf.cutoffHz)
        
        if (b is ExternalSessionAudioBackend) {
            b.statePushSuccessful = true
        }
    }

    private fun updateDiagnostics() {
        val b = backend
        if (b != null && b.isAttached) {
            val caps = mutableMapOf<String, DspCapability>()
            listOf("EQ", "BassBoost", "Loudness", "Haas", "Crossfeed", "PBE", "AFR", "Compressor", "Limiter", "Preamp", "ChannelConfig").forEach {
                caps[it] = b.getCapability(it)
            }
            diagnosticsInfo.value = DiagnosticsInfo(
                processingMode = b.backendName,
                activeSessionId = _activeSessionId.value,
                activePackage = _activePackageName.value,
                nativeEqBandCount = b.getNativeEqBands(),
                activeRoute = "Unknown",
                sampleRate = "Unknown",
                latencyMs = 0,
                capabilities = caps,
                errors = listOfNotNull(b.getLastError()),
                isRealBackend = b !is SystemRootAudioBackend,
                eqHasControl = (b as? ExternalSessionAudioBackend)?.hasEqControl == true,
                bassHasControl = (b as? ExternalSessionAudioBackend)?.hasBassControl == true,
                loudnessHasControl = (b as? ExternalSessionAudioBackend)?.hasLoudnessControl == true,
                statePushedSuccessfully = (b as? ExternalSessionAudioBackend)?.statePushSuccessful == true
            )
        } else {
            diagnosticsInfo.value = DiagnosticsInfo(
                processingMode = "None",
                errors = listOfNotNull(_lastError.value)
            )
        }
    }

    fun detachSession(sessionId: Int? = null) {
        // Ignore close requests for other sessions if we are happily attached to a different one
        if (sessionId != null && sessionId != _activeSessionId.value) {
            Log.d(TAG, "Ignoring detach request for session $sessionId because we are on ${_activeSessionId.value}")
            return
        }
        
        lastSessionEvent.value = "CLOSE session=${sessionId ?: "all"}"
        Log.d(TAG, "Detaching session ${sessionId ?: "all"}")
        release()
        
        // Re-attempt global mix fallback
        if (sessionId != 0) {
            attachToSession(0, "Global Mix Fallback")
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

    // -- State Updaters --
    
    fun setMasterEnabled(enabled: Boolean) {
        Log.d(TAG, "setMasterEnabled: $enabled")
        _dspState.update { it.copy(masterEnabled = enabled) }
        applyCurrentStateToBackend(backend)
    }

    fun updateHaasConfig(config: HaasConfig) {
        _dspState.update { it.copy(haas = config) }
        val bypass = !_dspState.value.masterEnabled
        backend?.setHaasSurround(!bypass && config.enabled, config.delayMs, config.amount, 0, 0, false, config.width)
    }

    fun updateCrossfeedConfig(config: CrossfeedConfig) {
        _dspState.update { it.copy(crossfeed = config) }
        backend?.setCrossfeed("Custom", config.directLevel, config.crossfeedLevel, 0, config.cutoffHz)
    }
    
    fun setEqEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
        val bypass = !_dspState.value.masterEnabled
        backend?.setEqEnabled(!bypass && enabled)
    }
    
    fun setBandLevel(bandIndex: Short, level: Short) {
        val updated = _eqBands.value.map { if (it.index == bandIndex) it.copy(gain = level.toInt()) else it }
        _eqBands.value = updated
        backend?.setEqBands(updated)
    }
    
    fun setBassEnabled(enabled: Boolean) {
        _bassEnabled.value = enabled
        val bypass = !_dspState.value.masterEnabled
        backend?.setBassEnabled(!bypass && enabled)
    }
    
    fun setBassStrength(strength: Int) {
        _bassStrength.value = strength
        backend?.setBassStrength(strength)
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        _loudnessEnabled.value = enabled
        val bypass = !_dspState.value.masterEnabled
        backend?.setLoudnessEnabled(!bypass && enabled)
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
        Log.d(TAG, "applyExtremeTest: Setting dramatic EQ for testing")
        if (_eqBands.value.isEmpty()) return
        
        // Ensure EQ and Master are enabled for the extreme test to work
        setMasterEnabled(true)
        setEqEnabled(true)
        
        val lowestBand = _eqBands.value.minByOrNull { it.centerFreq }
        val highestBand = _eqBands.value.maxByOrNull { it.centerFreq }
        val updated = _eqBands.value.map {
            when (it.index) {
                lowestBand?.index -> it.copy(gain = -1500)
                highestBand?.index -> it.copy(gain = 1500)
                else -> it.copy(gain = 0)
            }
        }
        _eqBands.value = updated
        backend?.setEqBands(updated)
    }
}
