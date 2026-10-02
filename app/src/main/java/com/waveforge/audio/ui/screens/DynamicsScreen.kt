package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waveforge.audio.engine.CompressorConfig
import com.waveforge.audio.engine.LimiterConfig
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicsScreen(viewModel: WaveForgeViewModel) {
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()
    val dspState by viewModel.dspState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DYNAMICS") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            WaveForgeCard {
                SectionHeader("COMPRESSOR")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Compressor"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Compressor")
                        Switch(
                            checked = dspState.compressor.enabled,
                            onCheckedChange = { viewModel.updateCompressorConfig(dspState.compressor.copy(enabled = it)) }
                        )
                    }
                    Text("Threshold: ${dspState.compressor.threshold} dB")
                    Slider(
                        value = dspState.compressor.threshold.toFloat(),
                        onValueChange = { viewModel.updateCompressorConfig(dspState.compressor.copy(threshold = it.roundToInt())) },
                        valueRange = -60f..0f
                    )
                    Text("Ratio: ${dspState.compressor.ratio}:1")
                    Slider(
                        value = dspState.compressor.ratio.toFloat(),
                        onValueChange = { viewModel.updateCompressorConfig(dspState.compressor.copy(ratio = it.roundToInt())) },
                        valueRange = 1f..20f
                    )
                    Text("Attack: ${dspState.compressor.attackMs} ms")
                    Slider(
                        value = dspState.compressor.attackMs.toFloat(),
                        onValueChange = { viewModel.updateCompressorConfig(dspState.compressor.copy(attackMs = it.roundToInt())) },
                        valueRange = 1f..200f
                    )
                    Text("Release: ${dspState.compressor.releaseMs} ms")
                    Slider(
                        value = dspState.compressor.releaseMs.toFloat(),
                        onValueChange = { viewModel.updateCompressorConfig(dspState.compressor.copy(releaseMs = it.roundToInt())) },
                        valueRange = 10f..1000f
                    )
                    Text("Makeup Gain: ${dspState.compressor.makeupGain} dB")
                    Slider(
                        value = dspState.compressor.makeupGain.toFloat(),
                        onValueChange = { viewModel.updateCompressorConfig(dspState.compressor.copy(makeupGain = it.roundToInt())) },
                        valueRange = 0f..24f
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            WaveForgeCard {
                SectionHeader("LIMITER")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Limiter"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Limiter")
                        Switch(
                            checked = dspState.limiter.enabled,
                            onCheckedChange = { viewModel.updateLimiterConfig(dspState.limiter.copy(enabled = it)) }
                        )
                    }
                    Text("Threshold: ${dspState.limiter.threshold} dB")
                    Slider(
                        value = dspState.limiter.threshold.toFloat(),
                        onValueChange = { viewModel.updateLimiterConfig(dspState.limiter.copy(threshold = it.roundToInt())) },
                        valueRange = -24f..0f
                    )
                    Text("Release: ${dspState.limiter.releaseMs} ms")
                    Slider(
                        value = dspState.limiter.releaseMs.toFloat(),
                        onValueChange = { viewModel.updateLimiterConfig(dspState.limiter.copy(releaseMs = it.roundToInt())) },
                        valueRange = 10f..1000f
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
