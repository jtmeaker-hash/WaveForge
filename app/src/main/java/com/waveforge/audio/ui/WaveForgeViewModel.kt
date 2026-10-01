package com.waveforge.audio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.waveforge.audio.engine.AudioEngine
import com.waveforge.audio.engine.EngineState
import com.waveforge.audio.engine.DiagnosticsInfo
import com.waveforge.audio.data.WaveForgePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WaveForgeViewModel(
    val engine: AudioEngine,
    val repository: WaveForgePreferencesRepository
) : ViewModel() {

    val engineState = engine.engineState.stateIn(viewModelScope, SharingStarted.Lazily, EngineState.WaitingForSession)
    val activeSessionId = engine.activeSessionId.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val activePackageName = engine.activePackageName.stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    val eqBands = engine.eqBands.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val eqEnabled = engine.eqEnabled.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val bassEnabled = engine.bassEnabled.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val bassStrength = engine.bassStrength.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val loudnessEnabled = engine.loudnessEnabled.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val loudnessGain = engine.loudnessGain.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val lastError = engine.lastError.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val diagnosticsInfo = engine.diagnosticsInfo.stateIn(viewModelScope, SharingStarted.Lazily, DiagnosticsInfo())
    val lastSessionEvent = engine.lastSessionEvent.stateIn(viewModelScope, SharingStarted.Lazily, "None")

    fun setEqEnabled(enabled: Boolean) = engine.setEqEnabled(enabled)
    fun setBandLevel(bandIndex: Short, level: Short) = engine.setBandLevel(bandIndex, level)
    fun setBassEnabled(enabled: Boolean) = engine.setBassEnabled(enabled)
    fun setBassStrength(strength: Int) = engine.setBassStrength(strength)
    fun setLoudnessEnabled(enabled: Boolean) = engine.setLoudnessEnabled(enabled)
    fun setLoudnessGain(gain: Int) = engine.setLoudnessGain(gain)
    
    fun resetEq() = engine.resetEq()
    fun applyExtremeTest() = engine.applyExtremeTest()
}

class WaveForgeViewModelFactory(
    private val engine: AudioEngine,
    private val repository: WaveForgePreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WaveForgeViewModel(engine, repository) as T
    }
}
