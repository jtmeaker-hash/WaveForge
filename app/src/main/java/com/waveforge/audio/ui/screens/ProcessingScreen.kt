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
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(viewModel: WaveForgeViewModel) {
    val masterBypassed by viewModel.masterBypassed.collectAsState()
    val bassStrength by viewModel.bassStrength.collectAsState()
    val loudnessGain by viewModel.loudnessGain.collectAsState()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val tempo by viewModel.tempo.collectAsState()

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
                SectionHeader("TONE & DYNAMICS")
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Bass Boost: $bassStrength", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = bassStrength.toFloat(),
                    onValueChange = { viewModel.setBassStrength(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = !masterBypassed
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loudness Enhancer (Preamp): $loudnessGain mB", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = loudnessGain.toFloat(),
                    onValueChange = { viewModel.setLoudnessGain(it.roundToInt()) },
                    valueRange = 0f..5000f,
                    enabled = !masterBypassed
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Virtualizer (Stereo Width): $virtualizerStrength", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = virtualizerStrength.toFloat(),
                    onValueChange = { viewModel.setVirtualizerStrength(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = !masterBypassed
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            WaveForgeCard {
                SectionHeader("PLAYBACK (FULL DSP PLAYER ONLY)")
                Text("Settings here only affect internal playback via Media3.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Pitch: $pitch", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = pitch,
                    onValueChange = { viewModel.setPitch(it) },
                    valueRange = 0.5f..2.0f,
                    enabled = !masterBypassed
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tempo: $tempo", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = tempo,
                    onValueChange = { viewModel.setTempo(it) },
                    valueRange = 0.5f..2.0f,
                    enabled = !masterBypassed
                )
            }
        }
    }
}
