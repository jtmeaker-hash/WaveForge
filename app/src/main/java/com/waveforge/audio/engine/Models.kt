package com.waveforge.audio.engine

enum class DspCapability {
    SUPPORTED_NATIVE,
    SUPPORTED_MAPPED,
    SUPPORTED_PCM_ONLY,
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
    val capabilities: Map<String, DspCapability> = emptyMap(),
    val errors: List<String> = emptyList()
)

sealed class EngineState {
    object Disabled : EngineState()
    object WaitingForSession : EngineState()
    data class Attached(val sessionId: Int, val packageName: String?, val mode: String) : EngineState()
    data class Error(val message: String) : EngineState()
}
