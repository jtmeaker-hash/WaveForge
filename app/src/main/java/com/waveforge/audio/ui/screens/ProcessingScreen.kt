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
import com.waveforge.audio.engine.StereoWidthConfig
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(viewModel: WaveForgeViewModel) {
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()
    val dspState by viewModel.dspState.collectAsState()
    val loudnessEnabled by viewModel.loudnessEnabled.collectAsState()
    val loudnessGain by viewModel.loudnessGain.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PROCESSING") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            WaveForgeCard {
                SectionHeader("STEREO WIDTH")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Stereo"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Stereo Width")
                        Switch(
                            checked = dspState.stereoWidth.enabled,
                            onCheckedChange = { viewModel.updateStereoWidthConfig(dspState.stereoWidth.copy(enabled = it)) }
                        )
                    }
                    Text("Strength: ${dspState.stereoWidth.strength} %")
                    Slider(
                        value = dspState.stereoWidth.strength.toFloat(),
                        onValueChange = { viewModel.updateStereoWidthConfig(dspState.stereoWidth.copy(strength = it.roundToInt())) },
                        valueRange = 0f..100f
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            WaveForgeCard {
                SectionHeader("PREAMP / MASTER GAIN")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Preamp"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Preamp")
                        Switch(
                            checked = loudnessEnabled,
                            onCheckedChange = { viewModel.setLoudnessEnabled(it) }
                        )
                    }
                    Text("Gain: ${loudnessGain} mB")
                    Slider(
                        value = loudnessGain.toFloat(),
                        onValueChange = { viewModel.setLoudnessGain(it.roundToInt()) },
                        valueRange = 0f..2000f
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            WaveForgeCard {
                SectionHeader("CHANNEL CONFIGURATION")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Channel"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
