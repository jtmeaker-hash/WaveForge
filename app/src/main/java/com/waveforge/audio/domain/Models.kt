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

data class EqBand(
    val index: Short,
    val centerFreq: Int,
    val minGain: Short,
    val maxGain: Short,
    var gain: Short
)

data class WfEqBand(
    val id: Int,
    val centerFreq: Int,
    val gainDb: Float
)
