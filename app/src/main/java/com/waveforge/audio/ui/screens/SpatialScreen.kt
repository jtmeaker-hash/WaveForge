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
import com.waveforge.audio.engine.CrossfeedConfig
import com.waveforge.audio.engine.HaasConfig
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpatialScreen(viewModel: WaveForgeViewModel) {
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()
    val dspState by viewModel.dspState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SPATIAL AUDIO") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            
            // HAAS SURROUND
            WaveForgeCard {
                SectionHeader("HAAS SURROUND")
                Spacer(modifier = Modifier.height(8.dp))
                val haasCap = diagnostics.capabilities["Haas"]?.name ?: "UNAVAILABLE_NO_SESSION"
                
                if (haasCap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.\nRequires WaveForge System-Wide DSP Engine.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Haas Surround")
                        Switch(
                            checked = dspState.haas.enabled,
                            onCheckedChange = { viewModel.updateHaasConfig(dspState.haas.copy(enabled = it)) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Delay: ${dspState.haas.delayMs} ms")
                    Slider(
                        value = dspState.haas.delayMs.toFloat(),
                        onValueChange = { viewModel.updateHaasConfig(dspState.haas.copy(delayMs = it.roundToInt())) },
                        valueRange = 0f..40f
                    )
                    
                    Text("Amount / Mix: ${dspState.haas.amount} %")
                    Slider(
                        value = dspState.haas.amount.toFloat(),
                        onValueChange = { viewModel.updateHaasConfig(dspState.haas.copy(amount = it.roundToInt())) },
                        valueRange = 0f..100f
                    )
                    
                    Text("Width: ${dspState.haas.width} %")
                    Slider(
                        value = dspState.haas.width.toFloat(),
                        onValueChange = { viewModel.updateHaasConfig(dspState.haas.copy(width = it.roundToInt())) },
                        valueRange = 0f..250f
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Delayed Channel:")
                    Row {
                        RadioButton(
                            selected = dspState.haas.delayedChannel == "Left",
                            onClick = { viewModel.updateHaasConfig(dspState.haas.copy(delayedChannel = "Left")) }
                        )
                        Text("Left", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
                        
                        RadioButton(
                            selected = dspState.haas.delayedChannel == "Right",
                            onClick = { viewModel.updateHaasConfig(dspState.haas.copy(delayedChannel = "Right")) }
                        )
                        Text("Right", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                    
                    Button(onClick = { viewModel.updateHaasConfig(HaasConfig()) }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Reset Haas")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // CROSSFEED
            WaveForgeCard {
                SectionHeader("CROSSFEED")
                Spacer(modifier = Modifier.height(8.dp))
                val cfCap = diagnostics.capabilities["Crossfeed"]?.name ?: "UNAVAILABLE_NO_SESSION"
                
                if (cfCap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.\nRequires WaveForge System-Wide DSP Engine.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Crossfeed")
                        Switch(
                            checked = dspState.crossfeed.enabled,
                            onCheckedChange = { viewModel.updateCrossfeedConfig(dspState.crossfeed.copy(enabled = it)) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Amount: ${dspState.crossfeed.amount} %")
                    Slider(
                        value = dspState.crossfeed.amount.toFloat(),
                        onValueChange = { viewModel.updateCrossfeedConfig(dspState.crossfeed.copy(amount = it.roundToInt())) },
                        valueRange = 0f..100f
                    )
                    
                    Text("Cutoff Frequency: ${dspState.crossfeed.cutoffHz} Hz")
                    Slider(
                        value = dspState.crossfeed.cutoffHz.toFloat(),
                        onValueChange = { viewModel.updateCrossfeedConfig(dspState.crossfeed.copy(cutoffHz = it.roundToInt())) },
                        valueRange = 200f..2000f
                    )
                    
                    Text("Direct Level: ${dspState.crossfeed.directLevel} dB")
                    Slider(
                        value = dspState.crossfeed.directLevel.toFloat(),
                        onValueChange = { viewModel.updateCrossfeedConfig(dspState.crossfeed.copy(directLevel = it.roundToInt())) },
                        valueRange = -12f..12f
                    )
                    
                    Text("Crossfeed Level: ${dspState.crossfeed.crossfeedLevel} dB")
                    Slider(
                        value = dspState.crossfeed.crossfeedLevel.toFloat(),
                        onValueChange = { viewModel.updateCrossfeedConfig(dspState.crossfeed.copy(crossfeedLevel = it.roundToInt())) },
                        valueRange = -24f..0f
                    )
                    
                    Button(onClick = { viewModel.updateCrossfeedConfig(CrossfeedConfig()) }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Reset Crossfeed")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
