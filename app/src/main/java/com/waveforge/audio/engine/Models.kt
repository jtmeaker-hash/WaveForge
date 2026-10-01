package com.waveforge.audio.engine

enum class DspCapability {
    SUPPORTED_NATIVE,
    SUPPORTED_MAPPED,
    SUPPORTED_PCM_ONLY,
    SUPPORTED_SYSTEM_WIDE,
    UNSUPPORTED_DEVICE,
    UNSUPPORTED_EXTERNAL_SESSION,
    UNAVAILABLE_NO_SESSION,
    ERROR_INITIALISING
}

data class EqBandRequest(
    val index: Short,
    val centerFreq: Int,
    val minGain: Int,
    val maxGain: Int,
    var gain: Int
)

data class DiagnosticsInfo(
    val processingMode: String = "None",
    val activeSessionId: Int? = null,
    val activePackage: String? = null,
    val nativeEqBandCount: Int = 0,
    val activeRoute: String = "Unknown",
    val sampleRate: String = "48 kHz",
    val latencyMs: Int = 0,
    val capabilities: Map<String, DspCapability> = emptyMap(),
    val errors: List<String> = emptyList()
)

sealed class EngineState {
    object Disabled : EngineState()
    object WaitingForSession : EngineState()
    data class Attached(val sessionId: Int?, val packageName: String?, val mode: String) : EngineState()
    data class Error(val message: String) : EngineState()
}

data class HaasConfig(
    val enabled: Boolean = false,
    val delayMs: Int = 15,
    val amount: Int = 50,
    val width: Int = 100,
    val delayedChannel: String = "Right"
)

data class CrossfeedConfig(
    val enabled: Boolean = false,
    val amount: Int = 50,
    val cutoffHz: Int = 700,
    val directLevel: Int = 0,
    val crossfeedLevel: Int = -6
)

data class DspState(
    val masterEnabled: Boolean = true,
    val preamp: Int = 0,
    val outputGain: Int = 0,
    val haas: HaasConfig = HaasConfig(),
    val crossfeed: CrossfeedConfig = CrossfeedConfig()
)
