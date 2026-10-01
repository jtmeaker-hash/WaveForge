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
import com.waveforge.audio.engine.EngineState
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: WaveForgeViewModel) {
    val engineState by viewModel.engineState.collectAsState()
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val bassEnabled by viewModel.bassEnabled.collectAsState()
    val loudnessEnabled by viewModel.loudnessEnabled.collectAsState()
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()

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
                SectionHeader("PROCESSING STATUS")
                Spacer(modifier = Modifier.height(8.dp))
                
                when (val state = engineState) {
                    is EngineState.Attached -> {
                        val hasCtrl = diagnostics.capabilities["EQ"]?.name?.contains("SUPPORTED") == true || diagnostics.capabilities["BassBoost"]?.name?.contains("SUPPORTED") == true
                        if (hasCtrl) {
                            Text("PROCESSING ACTIVE", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            Text("Mode: ${state.mode}", style = MaterialTheme.typography.bodyMedium)
                            Text("Package: ${state.packageName}", style = MaterialTheme.typography.bodyMedium)
                            Text("Session: ${state.sessionId}", style = MaterialTheme.typography.bodyMedium)
                            Text("EQ: ${if (eqEnabled) "On" else "Off"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Bass Boost: ${if (bassEnabled) "On" else "Off"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Loudness: ${if (loudnessEnabled) "On" else "Off"}", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("NO EFFECT CONTROL", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                            Text("An audio session was detected, but WaveForge does not currently control the Android audio effect instance.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    is EngineState.WaitingForSession -> {
                        Text("WAITING FOR AUDIO SESSION", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                        Text("Start playback in a compatible music app.\nWaveForge has settings ready but is not currently attached to an audio session.", style = MaterialTheme.typography.bodyMedium)
                    }
                    is EngineState.Error -> {
                        Text("ERROR", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    }
                    is EngineState.Disabled -> {
                        Text("DISABLED", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
