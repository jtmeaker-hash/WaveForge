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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(viewModel: WaveForgeViewModel) {
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val eqHasControl by viewModel.eqHasControl.collectAsState()

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
                    Text("Enable EQ", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = eqEnabled, onCheckedChange = { viewModel.setEqEnabled(it) })
                }
                if (!eqHasControl) {
                    Text("Engine lacks control. Changes will apply when session is active.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { viewModel.resetEq() }, modifier = Modifier.weight(1f)) {
                        Text("RESET TO FLAT")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.applyExtremeTest() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("EXTREME EQ TEST")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(eqBands) { band ->
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
