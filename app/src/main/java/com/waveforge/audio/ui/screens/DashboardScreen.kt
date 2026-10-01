package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: WaveForgeViewModel) {
    val engineState by viewModel.engineState.collectAsState()
    val source by viewModel.audioSource.collectAsState()
    val backend by viewModel.backendName.collectAsState()
    val sessionId by viewModel.activeSessionId.collectAsState()
    val hasControl by viewModel.hasControl.collectAsState()
    val masterBypassed by viewModel.masterBypassed.collectAsState()
    val error by viewModel.lastError.collectAsState()
    val hwBands by viewModel.hardwareBands.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WAVEFORGE", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text("AUDIO PROCESSING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            WaveForgeCard {
                SectionHeader("AUDIO ENGINE STATUS")
                val activeStr = if (hasControl && !masterBypassed) "ACTIVE" else "BYPASSED / INACTIVE"
                Text("Audio processing: $activeStr", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Audio source: $source", style = MaterialTheme.typography.bodyMedium)
                Text("Effect mode: $backend", style = MaterialTheme.typography.bodyMedium)
                Text("Audio session ID: $sessionId", style = MaterialTheme.typography.bodyMedium)
                Text("EQ engine: ${if (hwBands.isNotEmpty()) "Available" else "Unavailable"}", style = MaterialTheme.typography.bodyMedium)
                Text("Master Bypass: ${if (masterBypassed) "ON" else "OFF"}", style = MaterialTheme.typography.bodyMedium)
                Text("EQ has control: ${if (hasControl) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium)
                Text("Band count: ${hwBands.size}", style = MaterialTheme.typography.bodyMedium)
                if (hwBands.isNotEmpty()) {
                    Text("Band gain range: ${hwBands[0].minGain / 100} to ${hwBands[0].maxGain / 100} dB", style = MaterialTheme.typography.bodyMedium)
                }
                
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Last engine error: $error", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
