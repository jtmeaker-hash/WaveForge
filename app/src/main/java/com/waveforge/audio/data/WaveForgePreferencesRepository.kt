package com.waveforge.audio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.waveforge.audio.domain.AppAudioMode
import com.waveforge.audio.domain.DspBypassState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "waveforge_settings")

class WaveForgePreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val AUDIO_MODE = stringPreferencesKey("audio_mode")
        val MASTER_BYPASS = booleanPreferencesKey("master_bypass")
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false
    }

    val audioMode: Flow<AppAudioMode> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.AUDIO_MODE] ?: AppAudioMode.FULL_PLAYER.name
        try {
            AppAudioMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppAudioMode.FULL_PLAYER
        }
    }

    val dspBypassState: Flow<DspBypassState> = context.dataStore.data.map { prefs ->
        DspBypassState(isBypassed = prefs[Keys.MASTER_BYPASS] ?: false)
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setAudioMode(mode: AppAudioMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUDIO_MODE] = mode.name
        }
    }

    suspend fun setDspBypassState(state: DspBypassState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MASTER_BYPASS] = state.isBypassed
        }
    }
}
