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
    val sampleRate: String = "Unknown",
    val latencyMs: Int = 0,
    val capabilities: Map<String, DspCapability> = emptyMap(),
    val errors: List<String> = emptyList(),
    val isRealBackend: Boolean = false,
    val eqHasControl: Boolean = false,
    val bassHasControl: Boolean = false,
    val loudnessHasControl: Boolean = false,
    val statePushedSuccessfully: Boolean = false
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

data class CompressorConfig(
    val enabled: Boolean = false,
    val threshold: Int = -20,
    val ratio: Int = 4,
    val knee: String = "Hard",
    val attackMs: Int = 10,
    val releaseMs: Int = 100,
    val makeupGain: Int = 0
)

data class LimiterConfig(
    val enabled: Boolean = false,
    val threshold: Int = -2,
    val releaseMs: Int = 100
)

data class PbeConfig(
    val enabled: Boolean = false,
    val strength: Int = 50,
    val preCut: Float = 2.0f
)

data class AfrConfig(
    val enabled: Boolean = false,
    val mode: String = "Soft",
    val intensity: Int = 50
)

data class StereoWidthConfig(
    val enabled: Boolean = false,
    val strength: Int = 50
)

data class DspState(
    val masterEnabled: Boolean = true,
    val preamp: Int = 0,
    val outputGain: Int = 0,
    val haas: HaasConfig = HaasConfig(),
    val crossfeed: CrossfeedConfig = CrossfeedConfig(),
    val compressor: CompressorConfig = CompressorConfig(),
    val limiter: LimiterConfig = LimiterConfig(),
    val pbe: PbeConfig = PbeConfig(),
    val afr: AfrConfig = AfrConfig(),
    val stereoWidth: StereoWidthConfig = StereoWidthConfig()
)
