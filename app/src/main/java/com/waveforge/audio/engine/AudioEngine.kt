package com.waveforge.audio.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.os.Build
import android.util.Log
import com.waveforge.audio.domain.EqBand
import com.waveforge.audio.domain.WfEqBand
import com.waveforge.audio.engine.backends.AndroidAudioEffectBackend
import com.waveforge.audio.engine.backends.AudioProcessingBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngine(private val context: Context) {
    private val TAG = "WF_AUDIO"

    private var backend: AudioProcessingBackend? = null

    private val _engineState = MutableStateFlow("Disconnected")
    val engineState: StateFlow<String> = _engineState.asStateFlow()

    private val _audioSource = MutableStateFlow("Unknown")
    val audioSource: StateFlow<String> = _audioSource.asStateFlow()

    private val _activeSessionId = MutableStateFlow(-1)
    val activeSessionId: StateFlow<Int> = _activeSessionId.asStateFlow()

    private val _hasControl = MutableStateFlow(false)
    val hasControl: StateFlow<Boolean> = _hasControl.asStateFlow()

    private val _backendName = MutableStateFlow("None")
    val backendName: StateFlow<String> = _backendName.asStateFlow()

    private val _hardwareBands = MutableStateFlow<List<EqBand>>(emptyList())
    val hardwareBands: StateFlow<List<EqBand>> = _hardwareBands.asStateFlow()

    private val defaultFrequencies = listOf(31000, 62000, 125000, 250000, 500000, 1000000, 2000000, 4000000, 8000000, 16000000)
    private val _wfBands = MutableStateFlow<List<WfEqBand>>(
        defaultFrequencies.mapIndexed { index, freq -> WfEqBand(index, freq, 0f) }
    )
    val wfBands: StateFlow<List<WfEqBand>> = _wfBands.asStateFlow()

    private val _masterBypassed = MutableStateFlow(false)
    val masterBypassed: StateFlow<Boolean> = _masterBypassed.asStateFlow()

    val bassStrength = MutableStateFlow(0)
    val loudnessGain = MutableStateFlow(0)
    val virtualizerStrength = MutableStateFlow(0)
    
    val pitch = MutableStateFlow(1.0f)
    val tempo = MutableStateFlow(1.0f)

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val sessionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
            val pkg = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Unknown"
            
            when (intent.action) {
                AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                    Log.d(TAG, "external session opened for $pkg on session $sessionId")
                    if (sessionId != -1) {
                        attachToSession(sessionId, pkg)
                    }
                }
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                    if (sessionId == backend?.activeSessionId) {
                        Log.d(TAG, "session closed")
                        attachToGlobalFallback()
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(sessionReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(sessionReceiver, filter)
        }
        attachToGlobalFallback()
    }

    private fun attachToSession(sessionId: Int, pkg: String) {
        backend?.release()
        val newBackend = AndroidAudioEffectBackend(sessionId, pkg)
        if (newBackend.isAvailable) {
            backend = newBackend
            _activeSessionId.value = sessionId
            _audioSource.value = pkg
            _backendName.value = newBackend.backendName
            _engineState.value = "Connected"
            _hasControl.value = newBackend.hasControl
            applyAllSettings()
        } else {
            _engineState.value = "Failed to attach"
            _lastError.value = "Engine creation rejected"
        }
    }

    private fun attachToGlobalFallback() {
        backend?.release()
        val newBackend = AndroidAudioEffectBackend(0, "System Global")
        if (newBackend.isAvailable && newBackend.hasControl) {
            backend = newBackend
            _activeSessionId.value = 0
            _audioSource.value = "Global Mix"
            _backendName.value = newBackend.backendName
            _engineState.value = "Connected (Global)"
            _hasControl.value = newBackend.hasControl
            applyAllSettings()
        } else {
            backend = null
            _activeSessionId.value = -1
            _audioSource.value = "None"
            _backendName.value = "Unavailable"
            _engineState.value = "Disconnected"
            _hasControl.value = false
            _hardwareBands.value = emptyList()
            _lastError.value = "Global audio processing unavailable on this device"
        }
    }

    private fun applyAllSettings() {
        backend?.setMasterBypass(_masterBypassed.value)
        backend?.applyEqCurve(_wfBands.value)
        backend?.setBassStrength(bassStrength.value)
        backend?.setLoudnessGain(loudnessGain.value)
        backend?.setVirtualizerStrength(virtualizerStrength.value)
        backend?.setPitch(pitch.value)
        backend?.setTempo(tempo.value)
        
        backend?.let {
            _hardwareBands.value = it.getHardwareBands()
        }
    }

    fun setMasterBypass(bypassed: Boolean) {
        _masterBypassed.value = bypassed
        backend?.setMasterBypass(bypassed)
    }

    fun setWfBandGain(id: Int, gainDb: Float) {
        val updated = _wfBands.value.map { if (it.id == id) it.copy(gainDb = gainDb) else it }
        _wfBands.value = updated
        backend?.applyEqCurve(updated)
        backend?.let { _hardwareBands.value = it.getHardwareBands() }
    }
    
    fun applyExtremeTest() {
        val updated = _wfBands.value.map { band ->
            val hz = band.centerFreq / 1000
            band.copy(gainDb = if (hz < 250 || hz >= 4000) -15f else 15f)
        }
        _wfBands.value = updated
        backend?.applyEqCurve(updated)
        backend?.let { _hardwareBands.value = it.getHardwareBands() }
    }

    fun resetEq() {
        val updated = _wfBands.value.map { it.copy(gainDb = 0f) }
        _wfBands.value = updated
        backend?.applyEqCurve(updated)
        backend?.let { _hardwareBands.value = it.getHardwareBands() }
    }
    
    fun setBassStrength(strength: Int) {
        bassStrength.value = strength
        backend?.setBassStrength(strength)
    }

    fun setLoudnessGain(gain: Int) {
        loudnessGain.value = gain
        backend?.setLoudnessGain(gain)
    }

    fun setVirtualizerStrength(strength: Int) {
        virtualizerStrength.value = strength
        backend?.setVirtualizerStrength(strength)
    }
    
    fun setPitch(p: Float) {
        pitch.value = p
        backend?.setPitch(p)
    }

    fun setTempo(t: Float) {
        tempo.value = t
        backend?.setTempo(t)
    }
}
