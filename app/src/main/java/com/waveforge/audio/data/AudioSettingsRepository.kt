package com.waveforge.audio.data

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.waveforge.audio.domain.AppAudioMode
import com.waveforge.audio.domain.DspBypassState

interface AudioSettingsRepository {
    val audioMode: StateFlow<AppAudioMode>
    val dspBypassState: StateFlow<DspBypassState>

    suspend fun setAudioMode(mode: AppAudioMode)
    suspend fun setDspBypassState(state: DspBypassState)
}

class AudioSettingsRepositoryImpl : AudioSettingsRepository {
    private val _audioMode = MutableStateFlow(AppAudioMode.FULL_PLAYER)
    override val audioMode = _audioMode.asStateFlow()

    private val _dspBypassState = MutableStateFlow(DspBypassState(isBypassed = false))
    override val dspBypassState = _dspBypassState.asStateFlow()

    override suspend fun setAudioMode(mode: AppAudioMode) {
        _audioMode.value = mode
    }

    override suspend fun setDspBypassState(state: DspBypassState) {
        _dspBypassState.value = state
    }
}
