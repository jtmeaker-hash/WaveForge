package com.waveforge.audio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.waveforge.audio.engine.AudioEngine
import com.waveforge.audio.data.WaveForgePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WaveForgeViewModel(
    val engine: AudioEngine,
    val repository: WaveForgePreferencesRepository
) : ViewModel() {

    val engineState = engine.engineState.stateIn(viewModelScope, SharingStarted.Lazily, "Disconnected")
    val audioSource = engine.audioSource.stateIn(viewModelScope, SharingStarted.Lazily, "Unknown")
    val activeSessionId = engine.activeSessionId.stateIn(viewModelScope, SharingStarted.Lazily, -1)
    val hasControl = engine.hasControl.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val backendName = engine.backendName.stateIn(viewModelScope, SharingStarted.Lazily, "None")
    
    val hardwareBands = engine.hardwareBands.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val wfBands = engine.wfBands.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val masterBypassed = engine.masterBypassed.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val lastError = engine.lastError.stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    val bassStrength = engine.bassStrength.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val loudnessGain = engine.loudnessGain.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val virtualizerStrength = engine.virtualizerStrength.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val pitch = engine.pitch.stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)
    val tempo = engine.tempo.stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)

    fun setMasterBypass(bypassed: Boolean) = engine.setMasterBypass(bypassed)
    fun setWfBandGain(id: Int, gainDb: Float) = engine.setWfBandGain(id, gainDb)
    fun applyExtremeTest() = engine.applyExtremeTest()
    fun resetEq() = engine.resetEq()
    
    fun setBassStrength(s: Int) = engine.setBassStrength(s)
    fun setLoudnessGain(g: Int) = engine.setLoudnessGain(g)
    fun setVirtualizerStrength(s: Int) = engine.setVirtualizerStrength(s)
    fun setPitch(p: Float) = engine.setPitch(p)
    fun setTempo(t: Float) = engine.setTempo(t)
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
