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
    val bassEnabled by viewModel.bassEnabled.collectAsState()
    val bassStrength by viewModel.bassStrength.collectAsState()
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Bass Boost", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = bassEnabled, onCheckedChange = { viewModel.setBassEnabled(it) })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Strength: $bassStrength", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = bassStrength.toFloat(),
                    onValueChange = { viewModel.setBassStrength(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = bassEnabled
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            WaveForgeCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Loudness Enhancer", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = loudnessEnabled, onCheckedChange = { viewModel.setLoudnessEnabled(it) })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gain: $loudnessGain mB", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = loudnessGain.toFloat(),
                    onValueChange = { viewModel.setLoudnessGain(it.roundToInt()) },
                    valueRange = 0f..5000f,
                    enabled = loudnessEnabled
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            WaveForgeCard {
                SectionHeader("OTHER MODULES")
                Text("Crossfeed - Coming soon", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Compressor - Coming soon", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
