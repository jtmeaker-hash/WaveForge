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
import com.waveforge.audio.engine.PbeConfig
import com.waveforge.audio.engine.AfrConfig
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancementScreen(viewModel: WaveForgeViewModel) {
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()
    val dspState by viewModel.dspState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ENHANCEMENT") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            WaveForgeCard {
                SectionHeader("PERCEPTUAL BASS (PBE)")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Perceptual"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable PBE")
                        Switch(
                            checked = dspState.pbe.enabled,
                            onCheckedChange = { viewModel.updatePbeConfig(dspState.pbe.copy(enabled = it)) }
                        )
                    }
                    Text("Strength: ${dspState.pbe.strength} %")
                    Slider(
                        value = dspState.pbe.strength.toFloat(),
                        onValueChange = { viewModel.updatePbeConfig(dspState.pbe.copy(strength = it.roundToInt())) },
                        valueRange = 0f..100f
                    )
                    Text("Pre-cut: ${dspState.pbe.preCut} dB")
                    Slider(
                        value = dspState.pbe.preCut,
                        onValueChange = { viewModel.updatePbeConfig(dspState.pbe.copy(preCut = it)) },
                        valueRange = 0f..10f
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            WaveForgeCard {
                SectionHeader("AUDITORY FATIGUE REDUCTION")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Auditory"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable AFR")
                        Switch(
                            checked = dspState.afr.enabled,
                            onCheckedChange = { viewModel.updateAfrConfig(dspState.afr.copy(enabled = it)) }
                        )
                    }
                    Text("Mode:")
                    Row {
                        RadioButton(
                            selected = dspState.afr.mode == "Soft",
                            onClick = { viewModel.updateAfrConfig(dspState.afr.copy(mode = "Soft")) }
                        )
                        Text("Soft", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
                        RadioButton(
                            selected = dspState.afr.mode == "Strong",
                            onClick = { viewModel.updateAfrConfig(dspState.afr.copy(mode = "Strong")) }
                        )
                        Text("Strong", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
