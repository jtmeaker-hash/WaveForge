package com.waveforge.audio.domain

enum class AppAudioMode {
    FULL_PLAYER,
    EXTERNAL_SESSION
}

data class DspBypassState(
    val isBypassed: Boolean
)

data class AudioCapabilityReport(
    val isDynamicsProcessingSupported: Boolean,
    val isEqualizerSupported: Boolean,
    val maxBands: Int
)

data class OutputDeviceProfile(
    val id: String,
    val name: String,
    val type: Int
)

data class AudioPreset(
    val name: String,
    val bands: Map<Int, Float>
)
