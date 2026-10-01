package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun EqualizerScreen(viewModel: WaveForgeViewModel) {
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val bands by viewModel.eqBands.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EQUALIZER") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            WaveForgeCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Equalizer", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = eqEnabled, onCheckedChange = { viewModel.setEqEnabled(it) })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.resetEq() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reset EQ")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (bands.isEmpty()) {
                WaveForgeCard {
                    Text("EQ not supported on this device or session.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(bands) { band ->
                        WaveForgeCard(modifier = Modifier.padding(bottom = 8.dp)) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${band.centerFreq / 1000} Hz", style = MaterialTheme.typography.bodyMedium)
                                    Text("${band.gain / 100} dB", style = MaterialTheme.typography.bodyMedium)
                                }
                                Slider(
                                    value = band.gain.toFloat(),
                                    onValueChange = { viewModel.setBandLevel(band.index, it.roundToInt().toShort()) },
                                    valueRange = band.minGain.toFloat()..band.maxGain.toFloat(),
                                    enabled = eqEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
